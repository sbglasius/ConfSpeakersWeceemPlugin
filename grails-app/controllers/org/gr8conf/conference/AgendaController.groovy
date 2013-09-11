package org.gr8conf.conference

import grails.converters.JSON

class AgendaController {
	def agendaService
	def wcmContentRepositoryService


	def edit() {
		def agenda = Agenda.get(params.id)
		if(!agenda) {
			flash.message = "Content not found with id ${params.id}"
			redirect(controller: 'wcmRepository')
		} else if(!agenda.instanceOf(Agenda)) {
			flash.message = "Can only edit Agenda content, and content with this id ${params.id} is not an Agenda type"
			redirect(controller: 'wcmRepository')
		} else {
			return [agenda: agenda, unscheduledPresentations: findUnscheduledPresentatiosn(agenda)]
		}
	}


	def trackList() {
		def agenda = Agenda.get(params.id)
		List<LinkedHashMap<String, Object>> tracks = agendaService.buildTrackList(agenda)
		render tracks as JSON
	}




	def saveTrackList() {
		def agenda = Agenda.get(params.id)
		def json = [status: false]
		if(agenda) {
			def status = agendaService.saveTrackList(agenda, JSON.parse(params.tracks))
			json.status = status
		}
		render(json as JSON)
	}

	def saveTrackData() {
		def agenda = Agenda.get(params.id)
		def json = [ : ]
		if(agenda) {
			def id = agendaService.saveTrackData(agenda, JSON.parse(params.trackData))
			log.debug("Saved $id")
			json.id = id
		}
		render(json as JSON)
	}

	def removeObsoleteSchedules() {
		def agenda = Agenda.get(params.id)
		def json = [status: false]
		if(agenda) {
			agendaService.removeObsoleteSchedules(agenda, JSON.parse(params.currentIds))
			json.status = true
		}
		render(json as JSON)
	}


	private findUnscheduledPresentatiosn(Agenda agenda) {
		Set<AgendaTrack> agendaTracks = agenda.children
		Set<AgendaSchedule> agendaSchedules = agendaTracks*.children.flatten()
		def usedPresentations = agendaSchedules*.presentation

		def presentations = Presentation.createCriteria().list {
			eq('space', agenda.space)
			status {
				eq('code', 400) // Only show published presentations
			}
		}

		presentations = presentations - usedPresentations

		return presentations
	}
}
