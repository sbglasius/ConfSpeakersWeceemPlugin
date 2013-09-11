package org.gr8conf.conference

import org.weceem.content.WcmContent
import org.weceem.util.ContentUtils

class AgendaSchedule extends WcmContent {

	static icon = [plugin: "conf-speakers-weceem", dir: "images", file: "agenda-schedule.png"]

	String presentationId
	Presentation presentation
	Date scheduled
	Integer duration

	static constraints = {
		presentationId(nullable: true)
		presentation(nullable: true)
		scheduled(nullable: false)
		duration(min: 0)
	}

	static editors = {
		status(editor: 'StatusReadOnly')
		title(editor: 'ReadOnly')
		presentationId(editor: 'Presentation')
		presentation(hidden: true)
		scheduled()
		duration()
	}

	static overrideRequired = ['title': false,'status':false]


	public setPresentationId(String id) {
		if(id) {
			presentation = Presentation.get(id.toLong())
			title = presentation.title
			status = presentation.status
		}
	}


	String getPresentationId() {
		return presentation?.id
	}

	static transients = WcmContent.transients + ['summary', 'presentationId']


	Map getVersioningProperties() {
		def r = super.getVersioningProperties() + [:] /* map of property name -> value */
		return r
	}


	String getAliasURI() {
		def uri = (title?.size() < 30) ? title : title.substring(0, 30)
		return uri?.replaceAll(org.weceem.content.WcmContent.INVALID_ALIAS_URI_CHARS_PATTERN, '-')
	}


	boolean contentShouldBeCreated(WcmContent parentContent) {
		return parentContent?.instanceOf(Agenda) || parentContent?.instanceOf(AgendaTrack)
	}


	public String getTitleForMenu() {
		presentation?.title ?: title
	}


	public String getTitleForHTML() {
		presentation?.title ?: title
	}


	public String getSummary() {
		ContentUtils.summarize(presentation?.content ?: title, 100, '...')
	}


	boolean contentShouldAcceptChildren() { false }
}
