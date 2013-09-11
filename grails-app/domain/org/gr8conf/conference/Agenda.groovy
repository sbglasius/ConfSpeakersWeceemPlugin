package org.gr8conf.conference

import org.weceem.blog.WcmBlog
import org.weceem.content.WcmContent
import org.weceem.content.WcmTemplate

class Agenda extends WcmContent {
	WcmTemplate template
	String agenda
	Date firstDay
	Date lastDay

	static icon = [plugin: "conf-speakers-weceem", dir: "images", file: "agenda.png"]

	static searchable = {
		alias WcmBlog.name.replaceAll("\\.", '_')
		only = ['title', 'status']
	}


	Map getVersioningProperties() {
		def r = super.getVersioningProperties() + [
				template: template?.ident() // Is this right?
		]
		return r
	}


	static constraints = {
		template(nullable: true)
		agenda(nullable: true)
		firstDay(nullable: false)
		lastDay(nullable: false, validator: {val, obj ->
			val > obj.firstDay
		})
	}

	static mapping = {
		template cascade: 'all', lazy: false
	}

	static transients = WcmContent.transients + ['agenda','agendaTracks','agendaScheduleBreaks']

	Set<AgendaTrack> getAgendaTracks() {
		return children?.findAll {it.instanceOf(AgendaTrack)}
	}

	Set<AgendaScheduleBreak> getAgendaScheduleBreaks() {
		return children?.findAll {it.instanceOf(AgendaScheduleBreak)}
	}

	static editors = {
		agenda(editor: 'Agenda')
		firstDay(editor: 'DateOnly')
		lastDay(editor: 'DateOnly')
		template()
	}


	def beforeInsert() {
		adjustFirstAndLastDay()
	}


	def beforeUpdate() {
		adjustFirstAndLastDay()
	}


	private adjustFirstAndLastDay() {
		if(firstDay.hours || firstDay.minutes || firstDay.seconds) {
			firstDay = firstDay.clearTime()
		}

		if(lastDay.hours != 23 || lastDay.minutes != 59 || lastDay.seconds != 59) {
			lastDay.hours = 23
			lastDay.minutes = 59
			lastDay.seconds = 59
		}
	}
}
