package org.gr8conf.conference

import org.weceem.content.WcmContent

class AgendaScheduleBreak extends WcmContent {

	static icon = [plugin: "conf-speakers-weceem", dir: "images", file: "agenda-schedule-break.png"]

	Date scheduled
	Integer duration

	static constraints = {
		scheduled(nullable: false)
		duration(min: 0)
	}

	static editors = {
		status()
		title()
		scheduled()
		duration()
	}


	static transients = WcmContent.transients


	Map getVersioningProperties() {
		def r = super.getVersioningProperties() + [:] /* map of property name -> value */
		return r
	}


	String getAliasURI() {
		def uri = "break-${scheduled?.format('yyyy-MM-dd-HH-mm')}"
		return uri?.replaceAll(org.weceem.content.WcmContent.INVALID_ALIAS_URI_CHARS_PATTERN, '-')
	}


	boolean contentShouldBeCreated(WcmContent parentContent) {
		return parentContent?.instanceOf(Agenda) || parentContent?.instanceOf(AgendaTrack)
	}


	public String getTitleForMenu() {
		title
	}


	public String getTitleForHTML() {
		title
	}

	boolean contentShouldAcceptChildren() { false }
}
