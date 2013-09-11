package org.gr8conf.conference

import grails.converters.JSON
import org.weceem.content.WcmSpace

class AgendaTrackController {

	static allowedMethods = [update: "POST"]


	def list() {
		def space = WcmSpace.get(params.space)
		if(!space) {
			flash.message = "Space not found with id ${params.space}"
			redirect(controller: 'wcmRepository')
		} else {
			def agenda = Agenda.findBySpaceAndId(space, params.id)
			if(!agenda) {
				flash.message = "Agenda not found with id ${params.id}"
				redirect(controller: 'wcmRepository')
			} else {
				[agendaInstance: agenda]
			}
		}
	}


	def update() {
		log.debug(params)
		def ids = params.list('id')
		def removed = params.list('removed')
		def names = params.list('name')
		def rooms = params.list('room')
		def textColors = params.list('textColor')
		def backgroundColors = params.list('backgroundColor')
		def agenda = Agenda.get(params.agenda)

		def list = makeListOfMaps(ids, removed, names, rooms, textColors, backgroundColors)

		list.each { Map map ->
			def remove = map.remove('removed')
			def id = map.remove('id')
			map.agenda = agenda
			def track = agenda.tracks.find { it.id == id } ?: new AgendaTrack(map)
			if(remove) {
				removeTrack(agenda, track)
			} else {
				addOrUpdateTrack(agenda, track, map)
			}
		}


		if(agenda.validate() && agenda.save(flush: true)) {
			def html = g.render(plugin: "conf-speakers-weceem", template: "/agendaTrack/templates/currentTracks", model: [content: agenda])
			render([ok: true, version: agenda.version, trackList: html] as JSON)
		} else {
			render([errors: agenda.errors] as JSON)
		}
	}


	private removeTrack(Agenda agenda, AgendaTrack track) {
		if(track.isInUse) {
			log.debug("Reject remove track $track")
			track.errors.rejectValue('id', 'agendaTrack.in-use', 'This track is in use and cannot be deleted')
		} else {
			agenda.removeFromTracks(track)
			track.delete()
		}
	}



	private addOrUpdateTrack(Agenda agenda, AgendaTrack track, Map map) {
		if(!track.id) {
			if(map.name) {
				agenda.addToTracks(map)
			}
		} else {
			bindData(track, map)
		}
	}


	private List makeListOfMaps(List ids, List removed, List names, List rooms, List textColors, List backgroundColors) {
		def list = []
		for(def i = 0; i < ids.size(); i++) {
			def id = ids[i].size() > 0 ? ids[i].toLong() : null
			list << [
					id: id,
					removed: removed[i]?.toBoolean(),
					name: names[i],
					room: rooms[i],
					textColor: textColors[i],
					backgroundColor: backgroundColors[i]
			]
		}
		return list
	}
}
