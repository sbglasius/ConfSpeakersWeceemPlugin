package org.gr8conf.conference

class ConferenceTagLib {
	static namespace = "wcm"
	def agendaDataService

	def speakerImg = { attrs ->
		def node = attrs.node
		def style = attrs.remove('style') ?: ''
		def cssClass = attrs.remove('class') ?: ''
		def absolute = attrs.remove('absolute') ?: false
		if(node) {
			def imageLink = node.fileData ? g.createLink(mapping: 'speakerImage', id: node.id, absolute: absolute) : g.resource(plugin: 'conf-speakers-weceem', dir: 'images', file: 'john-doe.png', absolute: absolute)
			out << """<img src="${imageLink}" alt="${node.title}" style="${style}" class="${cssClass}"/>"""
		}
	}

	def agendaResources = { attrs ->
		out << g.render(plugin: 'conf-speakers-weceem', template: '/agenda/templates/agendaHeaders')
	}

	def agenda = { attrs ->
		def node = attrs.node
		if(!node.instanceOf(Agenda)) {
			throwTagError("Node must be of type Agenda")
		}
		Agenda agenda = node
		def elementId = attrs.elementId ?: "agenda_${System.nanoTime()}"
		def cssClass = attrs.'class' ?: ""
		def style = attrs.style ?: ""
		def hourHeight = attrs.hourHeight ?: 60

		def agendaData = addViewSizes(agendaDataService.agendaJson(agenda), hourHeight);

		out << g.render(plugin: 'conf-speakers-weceem', template: '/agenda/templates/agendaView', model: [
				elementId: elementId,
				cssClass: cssClass,
				style: style,
				space: agenda.space.aliasURI,
				agenda: agendaData
		]
		)
	}


	def agendaLegend = { attrs ->
		def node = attrs.node
		if(!node.instanceOf(Agenda)) {
			throwTagError("Node must be of type Agenda")
		}
		out << g.render(plugin: 'conf-speakers-weceem', template: '/agendaTrack/templates/currentTracks', model: [node: node])
	}


	def editorFieldSpeakerImgUpload = { attrs ->
		out << """${speakerImg(node: pageScope.content)}<br/>"""
		out << """<input type="file" name="${attrs.property}"/>"""
	}

	def editorFieldSpeakerUris = { attrs ->
		def speakers = Speaker.findAllBySpace(pageScope.content.space)
		def speakerName = { it?.title }
		def speakerAlias = { it?.aliasURI }
		String speakerUris = pageScope.content[attrs.property]
		def speakerUrisList = (speakerUris?.split(/[,;]/) ?: []) as List
		out << g.select(name: attrs.property, from: speakers, optionKey: speakerAlias, optionValue: speakerName, value: speakerUrisList, multiple: true)
	}

	def editorFieldPresentation = { attrs ->
		def presentations = Presentation.findAllBySpace(pageScope.content.space)
		def title = { Presentation presentation ->
			if(presentation.speakers) {
				return "${presentation.title} (speaker${presentation.speakers.size() > 1 ? 's' : ''}: ${presentation.speakers*.title.join(', ')})"
			} else {
				return presentation.title
			}
		}
		out << g.select(name: attrs.property, from: presentations, optionKey: "id", optionValue: title, value: pageScope.content[attrs.property])
	}

	def editorFieldAgenda = { attrs ->
		out << g.render(plugin: 'conf-speakers-weceem', template: '/agenda/templates/editorFieldAgenda', model: [content: pageScope.content, property: attrs.property])
	}

	def editorFieldTracks = { attrs ->
		out << g.render(plugin: 'conf-speakers-weceem', template: '/agendaTrack/templates/editorFieldTracks', model: [content: pageScope.content, property: attrs.property])
	}

	def editorFieldTrack = { attrs ->
		out << g.render(plugin: 'conf-speakers-weceem', template: '/agendaTrack/templates/editorFieldTrack', model: [content: pageScope.content, property: attrs.property])
	}

	def editorFieldColor = { attrs ->
		out << g.render(plugin: 'conf-speakers-weceem', template: '/agendaTrack/templates/editorFieldColor', model: [content: pageScope.content, property: attrs.property])
	}

	def editorResourcesColor = { attrs ->
		if(!pageScope.colorResources) {
			log.debug("Rendering resources for Color")
			out << g.render(plugin: 'conf-speakers-weceem', template: '/agendaTrack/templates/editorResourcesColor')
			pageScope.colorResources = true
		}
	}

	def editorFieldDateOnly = { attrs ->
		StringBuilder sb = new StringBuilder()
		def d = pageScope.content[attrs.property]
		def dval = d?.format('yyyy/MM/dd')
		sb << g.textField(name: attrs.property, size: 10, maxLength: 10, value: dval)

		out << bean.customField(beanName: 'content', property: attrs.property, noLabel: true) {
			out << sb
		}

		out << g.javascript([:]) {
			"""
\$(function(){
 \$('#${attrs.property.encodeAsJavaScript()}').datepicker({
     dateFormat: 'yy/mm/dd'
 })
})
"""
		}
	}

	def editorFieldStatusReadOnly = { attrs ->
		def status = pageScope.content[attrs.property]

		out << "${status.description} (Follows presentation status)"
	}

	private addViewSizes(Map agenda, int hourHeight) {
		agenda.days.each { day ->
			int startHour = day.start.hours
			int endHour = day.end.hours
			if(day.end.minutes > 0) {
				endHour++
			}
			day.agendaHours = makeHours(startHour, endHour, hourHeight)
			day.height = (endHour - startHour) * hourHeight;
			makeDay(day, hourHeight)
		}
	}

	private makeDay(Map day, int hourHeight) {
		makeBlock(day, day, hourHeight)
		day.offset = day.start.minutes * hourHeight / 60
		day.blocks.each { block ->
			makeBlock(day, block, hourHeight)
			block.tracks.each { track ->
				makeBlock(block, track, hourHeight)
				track.width = "${(100 / block.tracks.size())}%"
				track.schedules.each { schedule ->
					makeBlock(block, schedule, hourHeight)
					schedule.displayTime = "${schedule.start.format('HH:mm')}-${schedule.end.format('HH:mm')}"
				}
			}
		}
		day.breaks.each {
			makeBlock(day, it, hourHeight)
		}
	}

	private makeBlock(reference, block, hourHeight) {
		block.top = calcDuration(reference.start, block.start) * hourHeight / 60
		block.bottom = block.top + calcDuration(block.start, block.end) * hourHeight / 60
	}

	private calcDuration(Date start, Date end) {
		return (end.time - start.time) / 1000 / 60
	}

	private makeHours(int startHour, int endHour, int hourHeight) {
		return (startHour..endHour).inject([]) { list, hour ->
			list << [top: (hour - startHour) * hourHeight, text: "${hour}:00"]
			if(hour != endHour) list << [top: (hour - startHour) * hourHeight + hourHeight.intdiv(2)]
			return list
		}
	}

}
