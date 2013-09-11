package org.gr8conf.conference

import org.weceem.content.WcmContent

class AgendaTrack extends WcmContent {

	static icon = [plugin: "conf-speakers-weceem", dir: "images", file: "agenda-track.png"]
	static colorMatch = '#[0-9A-Fa-f]{6}'
	String room
	String block
	String colors = '#000000;#cccccc'

	static transients = ['textColor','backgroundColor']

	static constraints = {
		room(nullable: true)
		block(nullable: true, inList: ['block1','block2','block3','block4'])
		textColor(blank:  false, matches: colorMatch)
		backgroundColor(blank:  false, matches: colorMatch)
		colors(blank: false)
	}
	static editors = {
		textColor(editor: 'Color')
		backgroundColor(editor: 'Color')
		colors(hidden: true)
	}

	boolean contentShouldBeCreated(WcmContent parentContent) {
		return parentContent?.instanceOf(Agenda)
	}

	String getTextColor() {
		colors.split(/;/)[0]
	}


	void setTextColor(String color) {
		colors = "$color;${backgroundColor}"
	}


	String getBackgroundColor() {
		colors.split(/;/)[1]
	}


	void setBackgroundColor(String color) {
		colors = "$textColor;$color"
	}
}
