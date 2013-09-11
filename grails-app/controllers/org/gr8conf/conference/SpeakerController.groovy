package org.gr8conf.conference

import org.weceem.content.WcmContent

import javax.servlet.http.HttpServletResponse

class SpeakerController {
	def img = {
		log.debug(params)
		def wcmContent = WcmContent.get(params.id)
		log.debug "${wcmContent.class.name}"
		if(wcmContent instanceof Speaker && wcmContent.fileMimeType) {
			log.debug "found ${wcmContent.fileMimeType}"
			response.setContentType(wcmContent.fileMimeType)
			response.outputStream << wcmContent.fileData
		} else {
			log.debug "not found"
			response.sendError(HttpServletResponse.SC_NOT_FOUND)
		}
	}
}
