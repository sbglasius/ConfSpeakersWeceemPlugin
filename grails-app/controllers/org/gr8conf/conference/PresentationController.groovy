package org.gr8conf.conference

import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND

class PresentationController {
	def wcmContentRepositoryService


	def summary(String uri) {
		println "Presentation uri $uri"
		def space = wcmContentRepositoryService.findSpaceByURI(uri.split(/\//)[1])
		def schedule = wcmContentRepositoryService.findContentForPath(uri.split(/\//)[2..-1].join('/'),space)?.content
		if(schedule) {
			log.debug("Schedule: $schedule")
			render(plugin: 'conf-speakers-weceem', template: '/agenda/templates/summary', model: [schedule: schedule])
		} else {
			render(status: SC_NOT_FOUND)
		}
	}
}
