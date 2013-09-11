import grails.converters.JSON
import groovy.util.logging.Log4j
import org.gr8conf.conference.ConferencePropertyEditorRegistrar
import org.gr8conf.conference.DateUtils

@Log4j
class ConfSpeakersWeceemGrailsPlugin {
	// the plugin version
	def version = "0.1"
	// the version or versions of Grails the plugin is designed for
	def grailsVersion = "1.3.7 > *"
	// the other plugins this plugin depends on
	def dependsOn = [:]
	// resources that are excluded from plugin packaging
	def pluginExcludes = [
			"grails-app/views/error.gsp"
	]

	// TODO Fill in these fields
	def author = "Your name"
	def authorEmail = ""
	def title = "Plugin summary/headline"
	def description = '''\\
Brief description of the plugin.
'''

	// URL to the plugin's documentation
	def documentation = "http://grails.org/plugin/conf-speakers"

	def doWithWebDescriptor = { xml ->
	}

	def doWithSpring = {
		conferencePropertyEditorRegistrar(ConferencePropertyEditorRegistrar)
	}

	def doWithDynamicMethods = { ctx ->
		log.info("Register iso8601 format JSON date")
		JSON.registerObjectMarshaller(Date) { Date it ->
			return DateUtils.iso8601Format(it)
		}
	}

	def doWithApplicationContext = { applicationContext ->
	}

	def onChange = { event ->
		// watching is modified and reloaded. The event contains: event.source,
		// event.application, event.manager, event.ctx, and event.plugin.
	}

	def onConfigChange = { event ->
		// The event is the same as for 'onChange'.
	}
}
