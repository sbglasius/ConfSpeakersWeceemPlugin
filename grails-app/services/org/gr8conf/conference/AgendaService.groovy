package org.gr8conf.conference

import org.codehaus.groovy.grails.web.util.TypeConvertingMap
import org.weceem.content.WcmContent
import org.weceem.content.WcmStatus
import org.weceem.tags.WeceemTagLib

class AgendaService {

	static transactional = true

	def wcmContentRepositoryService
	def searchableService

	def grailsApplication


	List buildTrackList(Agenda agenda) {
		def defaultColors = (['#d4550f', '#9A9CFF', '#FAD165', '#e59a67', '#f2ccb2'] * 10).iterator()
		def dateFormat = "yyyy-MM-dd'T'HH:mm:ss'-01:00'"
		def agendaScheduleBreaks = agenda.agendaScheduleBreaks
		def agendaTracks = agenda.agendaTracks


		def tracks = [
				[
						textColor: '#eeeeee',
						backgroundColor: '#666666',
						ignoreTimezone: true,
						events: agendaScheduleBreaks.collect { schedule ->
							def end = schedule.scheduled.clone()
							end[Calendar.MINUTE] += schedule.duration
							[
									id: schedule.id,
									agendaScheduleType: schedule.class.simpleName,
									title: schedule.title,
									start: DateUtils.iso8601Format(schedule.scheduled),
									end: DateUtils.iso8601Format(end),
									allDay: false,
									status: schedule.status.code,
							]
						}
				]]

		agendaTracks.each { track ->
			def schedules = AgendaSchedule.findAllByParent(track)
			def trackInfo = [
					id: track.id,
					name: track.title,
					room: track.room,
					textColor: track.textColor ?: '#000000',
					backgroundColor: track.backgroundColor ?: defaultColors.next(),
					events: schedules.collect { schedule ->
						def end = schedule.scheduled.clone()
						end[Calendar.MINUTE] += schedule.duration

						[
								id: schedule.id,
								agendaScheduleType: schedule.class.simpleName,
								presentation: schedule.presentationId,
								title: schedule.title,
								url: createPresentationUrl(schedule.presentation),
								start: DateUtils.iso8601Format(schedule.scheduled),
								end: DateUtils.iso8601Format(end),
								allDay: false,
								status: schedule.status.code,
						]
					}
			]
			tracks << trackInfo
		}


		return tracks
	}


	String createPresentationUrl(Presentation presentation) {
		if(presentation) {
			WeceemTagLib wcm = grailsApplication.mainContext.getBean('org.weceem.tags.WeceemTagLib')
			return wcm.createLink(space: presentation.space.aliasURI, node: presentation)
		} else {
			return null
		}
	}


	boolean saveTrackList(Agenda agenda, tracks) {
		List<AgendaTrack> agendaTracks = agenda.agendaTracks.toList()
		List<WcmContent> obsoleteAgendaScheduleBreaks = agenda.agendaScheduleBreaks.toList()
		List<WcmContent> obsoleteAgendaSchedules = AgendaSchedule.findAllByParentInList(agendaTracks)
		searchableService.stopMirroring()
		tracks.each { trackData ->
			AgendaTrack agendaTrack = AgendaTrack.get(trackData.track)
			log.debug("Processing $trackData")
			if(trackData.agendaScheduleType == 'AgendaSchedule') {
				def agendaSchedule = createOrUpdateAgendaSchedule(agendaTrack, trackData)
				obsoleteAgendaSchedules.remove(agendaSchedule)
			} else if(trackData.agendaScheduleType == 'AgendaScheduleBreak') {
				def agendaScheduleBreak = createOrUpdateAgendaScheduleBreak(agenda, trackData)
				obsoleteAgendaScheduleBreaks.remove(agendaScheduleBreak)
			} else {
			}
		}
		if(obsoleteAgendaSchedules) {
			log.debug("Removing obsolete agendaSchedules: ${obsoleteAgendaSchedules*.title}")
			wcmContentRepositoryService.deleteNodes(obsoleteAgendaSchedules)
		}

		searchableService.startMirroring()
		return true
	}


	Long saveTrackData(Agenda agenda, trackData) {
		AgendaTrack agendaTrack = AgendaTrack.get(trackData.track)
		if(trackData.agendaScheduleType == 'AgendaSchedule') {
			def agendaSchedule = createOrUpdateAgendaSchedule(agendaTrack, trackData)
			return agendaSchedule.id
		} else if(trackData.agendaScheduleType == 'AgendaScheduleBreak') {
			def agendaScheduleBreak = createOrUpdateAgendaScheduleBreak(agenda, trackData)
			return agendaScheduleBreak.id
		} else {
			log.debug("Unknown type: ${trackData}")
			return -1
		}
	}


	void removeObsoleteSchedules(Agenda agenda, def currentIds) {
		List<AgendaTrack> agendaTracks = agenda.agendaTracks.toList()
		List<WcmContent> obsolete = agenda.agendaScheduleBreaks.toList() + AgendaSchedule.findAllByParentInList(agendaTracks)
		currentIds.each { id ->
			def node = WcmContent.get(id)
			obsolete.remove(node)
		}
		if(obsolete) {
			log.debug("Removing obsolete agendaSchedules: ${obsolete.collect() {"${it.title} (${it.class.simpleName})"}}")
			wcmContentRepositoryService.deleteNodes(obsolete)
		}
	}


	private WcmContent createOrUpdateAgendaSchedule(AgendaTrack agendaTrack, def trackData) {
		if(trackData.id) {
			AgendaSchedule agendaSchedule = AgendaSchedule.get(trackData.id)
			log.debug("Updating agendaSchedule: ${agendaSchedule.title} ${trackData}")
			agendaSchedule.scheduled = DateUtils.getDateFromString(trackData.start)
			agendaSchedule.duration = trackData.duration.toInteger()
			if(!(agendaSchedule in agendaTrack.children)) {
				log.debug("Move ${agendaSchedule.title} to ${agendaTrack.title}")
				wcmContentRepositoryService.moveNode(agendaSchedule, agendaTrack, 0)
			}
			return agendaSchedule
		} else {
			def presentation = Presentation.get(trackData.presentation)
			log.debug("Create new agendaSchedule: ${presentation.title}")
			def agendaSchedule = wcmContentRepositoryService.createNode(AgendaSchedule.class.name, [
					title: presentation.title,
					status: presentation.status,
					aliasURI: presentation.aliasURI,
					presentation: presentation,
					scheduled: DateUtils.getDateFromString(trackData.start),
					duration: trackData.duration,
					parent: agendaTrack,
					space: agendaTrack.space,
			] as TypeConvertingMap)
			return agendaSchedule
		}
	}


	private WcmContent createOrUpdateAgendaScheduleBreak(Agenda agenda, Map trackData) {
		if(trackData.id) {
			AgendaScheduleBreak agendaScheduleBreak = AgendaScheduleBreak.get(trackData.id)
			agendaScheduleBreak.scheduled = DateUtils.getDateFromString(trackData.start)
			agendaScheduleBreak.duration = trackData.duration.toInteger()
			agendaScheduleBreak.title = trackData.title
			agendaScheduleBreak.save()
			return agendaScheduleBreak
		} else {
			log.debug("Create new agendaScheduleBreak: ${trackData.title}")
			def status = WcmStatus.findByCode(400)
			def agendaSchedule = wcmContentRepositoryService.createNode(AgendaScheduleBreak.class.name, [
					title: trackData.title,
					status: status,
					scheduled: DateUtils.getDateFromString(trackData.start),
					duration: trackData.duration,
					parent: agenda,
					space: agenda.space,
			] as TypeConvertingMap)
			return agendaSchedule
		}
	}
}
