package org.gr8conf.conference
import groovy.util.logging.Log4j

@Log4j
class DateUtils {
	static iso8601Format(Date date) {
		if(date) {
			String iso8601 = date.format("yyyy-MM-dd'T'HH:mm:ss")
			String timeZone = date.format('Z')
			return "${iso8601}${timeZone[0..-3]}:${timeZone[-2..-1]}"
		}
		return null
	}

	static Date getDateFromString(String stringDate) {
		Date.parse("yyyy-MM-dd HH:mm", stringDate)
	}
}
