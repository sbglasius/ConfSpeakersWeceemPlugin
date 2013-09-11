package org.gr8conf.conference

import org.springframework.web.multipart.MultipartFile
import org.weceem.content.WcmContent
import org.weceem.util.ContentUtils
import org.weceem.util.MimeUtils

class Speaker extends WcmContent {
	def wcmContentRepositoryService

	static icon = [plugin: "conf-speakers-weceem", dir: "images", file: "speaker.png"]
	static standaloneContent = true

	static searchable = {
		alias Speaker.name.replaceAll("\\.", '_')

		only = ['company','content', 'title']
	}


	String getMimeType() { "text/html" }


	MultipartFile uploadedFile

	String company
	String website_url
	String email
	String twitter
	String content
	String notes = ''
	String fileImage
	String fileMimeType

	static mapping = {
		columns {
			content type: 'text'
			notes type: 'text'
			fileImage sqlType: 'longtext'
		}
	}

	static constraints = {
		email blank: false, email: true
		twitter blank: true, nullable: true
		company blank: false
		website_url nullable: true, url: true
		content blank: false, maxSize: WcmContent.MAX_CONTENT_SIZE
		notes nullable: true, blank: true, maxSize: WcmContent.MAX_CONTENT_SIZE
		uploadedFile nullable: true
		fileImage nullable: true
		fileMimeType nullable: true, blank: true
	}

	/**
	 * Must be overriden by content types that can represent their content as text.
	 * Used for search results and versioning
	 */

	public String getContentAsText() {
		ContentUtils.htmlToText(content)
	}

	/**
	 * Should be overriden by content types that can represent their content as HTML.
	 * Used for wcm:content tag (content rendering)
	 */

	public String getContentAsHTML() {
		content
	}


	@SuppressWarnings("GroovyAssignabilityCheck")
	static editors = {
		email(editor: 'String')
		twitter(editor: 'String')
		company(editor: 'String')
		website_url(editor: 'String')
		content(editor: 'RichHTML')
		notes(editor: 'LongString', group: 'extra')
		uploadedFile(editor: 'SpeakerImgUpload')
		fileData(hidden: true)
		fileImage(hidden: true)
		fileMimeType(hidden: true)
		presentations(hidden: true)
	}

	static transients = WcmContent.transients + ['uploadedFile', 'presentations', 'fileData']

	byte[] getFileData() {
		if(fileImage) {
			fileImage.decodeBase64()
		}
	}

	void setFileData(byte[] data) {
		fileImage = data.encodeBase64()
	}

	List<Presentation> getPresentations() {
		Presentation.findAllBySpace(this.space).findAll { presentation -> this in presentation.speakers }
	}


	boolean contentShouldBeCreated(WcmContent parentContent) {
		return parentContent?.instanceOf(Speakers)
	}


	boolean contentShouldAcceptChildren() { false }


	void setUploadedFile(MultipartFile file) {
		if(file.bytes) {
			fileData = file.bytes
			fileMimeType = file.contentType ?: MimeUtils.getDefaultMimeType(file.originalFilename)
		}
	}


	Map getVersioningProperties() {
		def r = super.getVersioningProperties() + [
				email: email,
				twitter: twitter,
				company: company,
				website_url: website_url,
				content: content,
				notes: notes
		]
		return r
	}


	public String getSummary() {
		ContentUtils.summarize(contentAsText, 100, '...')
	}
}
