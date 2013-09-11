package org.gr8conf.conference

import org.weceem.content.WcmContent
import org.weceem.util.ContentUtils

/**
 * This is your Weceem content domain class. See the documentation at http://weceem.org for full details
 *
 * Summary: Add normal GORM properties for your custom content fields, including any relationships you need.
 * For non-standard types or associations you will need to implement custom wcm:editorXXXX tags to provide
 * editors for these. See the Weceem Plugin source for details (EditorTagLib.groovy)
 */
class Presentation extends WcmContent {
	def wcmContentRepositoryService

	static icon = [plugin: "conf-speakers-weceem", dir: "images", file: "presentation.png"]

	/* Add your custom content fields here */
	String content

	String speakerUris

	static constraints = {
		content(nullable: false, maxSize: WcmContent.MAX_CONTENT_SIZE)
		speakerUris(nullable: true)
	}

	static editors = {
		content(editor: 'RichHTML')
		speakerUris(editor: 'SpeakerUris')
		speakers(hidden: true)
	}
	static transients = WcmContent.transients + ['summary', 'speakers']


	public String getContentAsText() {
		ContentUtils.htmlToText(content)
	}


	public String getContentAsHTML() {
		content
	}


	Map getVersioningProperties() {
		def r = super.getVersioningProperties() + [:] /* map of property name -> value */
		return r
	}


	String getMimeType() {
		"text/html; charset=UTF-8"
	}


	boolean contentShouldBeCreated(WcmContent parentContent) {
		return parentContent?.instanceOf(Presentations)
	}


	public String getTitleForMenu() {
		title
	}


	public String getTitleForHTML() {
		title
	}


	public String getSummary() {
		ContentUtils.summarize(contentAsText, 100, '...')
	}

	String getSpeakerUris() {
		return this.speakerUris
	}

	void setSpeakerUris(List speakerUris) {
		this.speakerUris = speakerUris.join(',')
	}

	void setSpeakerUris(String speakerUris) {
		this.speakerUris = speakerUris
	}


	List<Speaker> getSpeakers() {
		def uris = speakerUris?.split(/[,;]/) ?: []
		return uris.collect { uri ->
			return Speaker.findBySpaceAndAliasURI(this.space, uri)
		}
	}

	def beforeDelete() {
		AgendaSchedule.withNewSession {
			AgendaSchedule.findByPresentation(this)?.delete()
		}
	}
}
