package org.gr8conf.conference
import grails.converters.JSON
import org.weceem.content.WcmContent
import org.weceem.content.WcmSpace
import org.weceem.content.WcmStatus

import javax.servlet.http.HttpServletResponse

class ScheduleAppController {

	def agendaDataService
	def wcmContentRepositoryService

	private activeChild = { it.status == WcmStatus.findByCode(400) }

	def getJson(String space, String uri) {
		log.debug("Space: $space, URI: $uri")

		def content = getContentFromUri(space, uri)
		if(!content) {
			response.sendError(HttpServletResponse.SC_NO_CONTENT, 'Selected uri is not found')
			return
		}

		def contentType = content.class.simpleName.toLowerCase()

		response.setHeader('Access-Control-Allow-Origin', request.getHeader("Origin"))
		response.setHeader('Access-Control-Allow-Methods', 'POST, PUT, GET, OPTIONS, PATCH')
		response.setHeader('Access-Control-Allow-Credentials', 'true')
		response.setHeader('Access-Control-Allow-Headers', 'origin, x-requested-with, accept')

		log.debug("ContentType: $contentType")

		def closure = agendaDataService."${contentType}Json"
		def json = closure.call(content)
		render json as JSON
	}

	def crossdomain() {
		render(contentType: 'text/xml') {
			'cross-domain-policy' {
				'site-control'('permitted-cross-domain-policies': "master-only")
				'allow-access-from'(domain: '*')
				'allow-http-request-headers-from'(domain: '*', headers: '*')
			}
		}
	}



	private getContentFromUri(String spaceName, String uri) {
		WcmSpace space = wcmContentRepositoryService.findSpaceByURI(spaceName)

		def contentInfo = wcmContentRepositoryService.findContentForPath(uri, space)
		WcmContent content = contentInfo?.content
		log.debug("Found content: $contentInfo in $space")
		return content
	}
}
