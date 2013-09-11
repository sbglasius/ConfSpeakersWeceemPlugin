package org.gr8conf.conference
  
import grails.converters.JSON

import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND

class AgendaViewController {
	def wcmContentRepositoryService


	def summary(String uri) {
		println "Presentation uri $uri"
		def space = wcmContentRepositoryService.findSpaceByURI(uri.split(/\//)[1])
		def schedule = wcmContentRepositoryService.findContentForPath(uri.split(/\//)[2..-1].join('/'),space)?.content
		if(schedule) {
			log.debug("Schedule: $schedule")
			def map = [content: g.render(plugin: 'conf-speakers-weceem', template: '/agenda/templates/summary', model: [schedule: schedule])]
			render map as JSON
		} else {
			render(status: SC_NOT_FOUND)
		}
	}
}
