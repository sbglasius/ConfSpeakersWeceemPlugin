package org.gr8conf.conference

import org.codehaus.groovy.grails.core.io.ResourceLocator
import org.codehaus.groovy.grails.web.mapping.LinkGenerator
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.core.io.Resource
import org.weceem.content.WcmContent
import org.weceem.content.WcmSpace
import org.weceem.content.WcmStatus

import javax.imageio.ImageIO
import java.awt.*
import java.awt.image.BufferedImage
import java.util.List

class AgendaDataService implements ApplicationContextAware {
    ApplicationContext applicationContext
    def servletContext
	def wcmContentRepositoryService
	LinkGenerator grailsLinkGenerator
    ResourceLocator grailsResourceLocator


	private activeChild = { it?.status == WcmStatus.findByCode(400) }

	@SuppressWarnings("GroovyUnusedDeclaration")
	public speakerJson = { Speaker speaker ->
		mapSpeaker(speaker)
	}

	@SuppressWarnings("GroovyUnusedDeclaration")
	public speakersJson = { Speakers speakers ->
		def listOfSpeakers = speakers.children.findAll(activeChild)
		[
				uri: makeLink(speakers),
				speakers: mapSpeakers(listOfSpeakers)
		]
	}

	@SuppressWarnings("GroovyUnusedDeclaration")
	public presentationJson = { Presentation presentation ->
		return mapPresentation(presentation)
	}

	@SuppressWarnings("GroovyUnusedDeclaration")
	public presentationsJson = { Presentations presentations ->
		def listOfPresentations = presentations.children.findAll(activeChild)
		[
				uri: makeLink(presentations),
				presentations: mapPresentations(listOfPresentations)
		]
	}

	@SuppressWarnings("GroovyUnusedDeclaration")
	public agendaJson = { Agenda agenda ->
		def activeAgendaTracks = agenda.children.findAll(activeChild).findAll { it.instanceOf(AgendaTrack) }
		def activeAgendaBreaks = agenda.children.findAll(activeChild).findAll { it.instanceOf(AgendaScheduleBreak) }
		mapBasicInfo(agenda) << [
				//tracks: mapTracks(agenda.children.findAll(activeChild).findAll { it.instanceOf(AgendaTrack) }),
				days: mapDays(activeAgendaTracks, activeAgendaBreaks)
		]
	}

	private getContentFromUri(String spaceName, String uri) {
		WcmSpace space = wcmContentRepositoryService.findSpaceByURI(spaceName)

		def contentInfo = wcmContentRepositoryService.findContentForPath(uri, space)
		WcmContent content = contentInfo?.content
		log.debug("Found content: $contentInfo in $space")
		return content
	}


	private mapPresentation = { Presentation presentation, boolean brief = false ->
		def map = mapBasicInfo(presentation) << [
				name: presentation.title,
                speakers: presentation.speakers.findAll(activeChild).collect {
           						[
           								uri: makeLink(it),
           								name: it.title
           						]
           					}
		]
		if(!brief) {
			map << [
					abstract: presentation.contentAsHTML,
			]
		}
		return map
	}

	private mapPresentations(Collection<Presentation> presentations) {
		presentations.collect(mapPresentation).sort { it.name }
	}

	private mapSpeaker = { Speaker speaker ->
        def image = speaker.fileImage?.decodeBase64()
        if(!image) {
            Resource resource = grailsResourceLocator.findResourceForURI("/images/john-doe.png")
            image = resource.file.bytes
        }
		mapBasicInfo(speaker) << [
				name: speaker.title,
				biography: speaker.contentAsHTML,
				company: speaker.company,
				websiteUrl: speaker.website_url,
				image: image ? "data:image/jpg;base64,${createImageThumb(image).encodeBase64()}" : null,
				twitter: speaker.twitter,
				presentations: speaker.presentations.findAll(activeChild).collect {
					[
							uri: makeLink(it),
							name: it.title
					]
				}
		]
	}

    private createImageThumb(byte[] bytes) {
        def output = new ByteArrayOutputStream()

        new ByteArrayInputStream(bytes).withStream { InputStream inStream ->
            BufferedImage sourceImage = ImageIO.read(inStream);
            Image thumbnail = sourceImage.getScaledInstance(105, -1, Image.SCALE_SMOOTH);
            BufferedImage bufferedThumbnail = new BufferedImage(thumbnail.getWidth(null),
                                                                thumbnail.getHeight(null),
                                                                BufferedImage.TYPE_INT_RGB);
            bufferedThumbnail.getGraphics().drawImage(thumbnail, 0, 0, null);
            ImageIO.write(bufferedThumbnail, "jpeg", output);

        }
        return output.toByteArray()
    }


	private mapSpeakers(Collection<Speaker> speakers) {
		speakers.collect(mapSpeaker).sort { it.name }
	}



	private mapTracks(Collection<AgendaTrack> agendaTracks) {
		agendaTracks.collect { mapTrack(it) }
	}

	private mapTrack(AgendaTrack agendaTrack) {
		mapBasicInfo(agendaTrack) << [
				room: agendaTrack.room,
				color: agendaTrack.colors,
				backgroundColor: agendaTrack.backgroundColor,
				schedules: agendaTrack.children.findAll(activeChild).collect { mapAgendaSchedule(it) }
		]
	}

	private mapDays(Collection<AgendaTrack> agendaTracks, Collection<AgendaScheduleBreak> agendaScheduleBreaks) {
		def allSchedules = mapAllSchedules(agendaTracks)
		return groupSchedulesByDaysAndTracks(allSchedules, agendaScheduleBreaks)
	}

	private groupSchedulesByDaysAndTracks(ArrayList<AgendaSchedule> agendaSchedules, Collection<AgendaScheduleBreak> agendaScheduleBreaks) {
		log.debug("Number of agendaSchedules: ${agendaSchedules.size()}")
		def days = agendaSchedules.groupBy { it.scheduled.clone().clearTime() }
		def breaks = agendaScheduleBreaks.groupBy { it.scheduled.clone().clearTime() }

		def result = days.collect { day, schedules ->
			def breaksByDay = breaks.get(day)?.sort { it.scheduled }
			def tracks = groupScheduleByTrack(schedules)
			def tracksByBlock = mapBlocks(tracks)
			def mappedBreaks = mapBreaks(breaksByDay)
			def startTimes = tracksByBlock*.start + mappedBreaks*.start
			def endTimes = tracksByBlock*.end + mappedBreaks*.end
			log.debug("StartTimes: $startTimes")
			log.debug("  EndTimes: $endTimes")
			[
					day: day,
					start:  startTimes.min(),
					end:  endTimes.max(),
					blocks: tracksByBlock,
					breaks: mappedBreaks
			]
		}
		return result.sort { it.day }
	}

	private mapBlocks(tracks) {
		def grouped = groupByBlocks(tracks)

		def result = grouped.collect {
			[
					start: it*.start.min(),
					end:  it*.end.max(),
					names: it*.name,
					tracks:  it
			]
		}

		return result
	}

	private groupScheduleByTrack(List<AgendaSchedule> agendaSchedules) {
		def groupedByTrack = agendaSchedules.groupBy { it.parent }
		groupedByTrack.collect { AgendaTrack track, List<AgendaSchedule> schedules ->
			def start = schedules*.scheduled.min()
			def end = schedules.collect {calculateEnd(it) }.max()
			mapBasicInfo(track) << [
					name: track.title,
					room: track.room,
					textColor: track.textColor,
					backgroundColor: track.backgroundColor,
					start: start,
					end:  end,
					schedules: schedules.collect(mapAgendaSchedule)
			]
		}
	}

	private ArrayList<AgendaSchedule> mapAllSchedules(Collection<AgendaTrack> agendaTracks) {
		def allChildren = agendaTracks*.children.flatten()
		return allChildren.findAll(activeChild)
	}

	private mapAgendaSchedule = { AgendaSchedule agendaSchedule ->
		mapBasicInfo(agendaSchedule) << [
				title: agendaSchedule.title,
				presentation: mapPresentation(agendaSchedule.presentation, true)
		] << mapSchedule(agendaSchedule)
	}

	private mapBreaks(Collection<AgendaScheduleBreak> agendaScheduleBreaks) {
		agendaScheduleBreaks.collect { mapBreak(it) }
	}

	private mapBreak(AgendaScheduleBreak agendaScheduleBreak) {
		[
				uri: makeLink(agendaScheduleBreak),
				title: agendaScheduleBreak.title,
		] << mapSchedule(agendaScheduleBreak)
	}


	private mapSchedule(def schedule) {
		[
				start: schedule.scheduled,
				end: calculateEnd(schedule),
				duration: schedule.duration
		]
	}

	private Date calculateEnd(schedule) {
		def end = schedule.scheduled.clone() as Date
		end[Calendar.MINUTE] += schedule.duration
		return end
	}

	private mapBasicInfo(WcmContent content) {
		[
				uri: makeLink(content),
				lastModified: content.lastModified,
		]
	}

	private List groupByBlocks(def init) {
		init.sort { a, b -> a.start <=> b.start ?: a.end <=> b.end }.inject([]) { list, map ->
			def post = list.find { sublist ->
				def start = sublist*.start.min()
				def end = sublist*.end.max()
				return start < map.start && end > map.start ||
						start < map.end && end > map.end ||
						start >= map.start && end <= map.end
			}
			if(post) {
				post << map
			} else {
				list << [map]
			}
			return list
		}
	}

	private String makeLink(WcmContent content) {
		def base = grailsLinkGenerator.link(mapping: 'app')
		grailsLinkGenerator.link(mapping: 'app', params: [space: content.space?.aliasURI, uri: content.absoluteURI]) - base
	}
}
