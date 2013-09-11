class ConfUrlMappings {

	static mappings = { appContext ->
		def ctx
		// Look for Grails 2.0 context arg
		if(appContext) {
			ctx = appContext
		} else {
			// Static holders are our only choice pre-2.0
			//noinspection GrDeprecatedAPIUsage
			ctx = org.codehaus.groovy.grails.commons.ApplicationHolder.application.mainContext
		}

		def config = ctx.grailsApplication.config

		final ADMIN_PREFIX = (config.weceem.admin.prefix instanceof String) ?
			config.weceem.admin.prefix : 'wcm/admin'

		final TOOLS_PREFIX = (config.weceem.tools.prefix instanceof String) ?
			config.weceem.tools.prefix : 'wcm'

		final MOBILE_PREFIX = (config.weceem.mobile.prefix instanceof String) ?
		    config.weceem.mobile.prefix : 'mobile'

		println("ADMIN_PREFIX: $ADMIN_PREFIX")
		def adminURI = "/${ADMIN_PREFIX}"

		delegate.(adminURI + "/agenda/$action?")(controller: 'agenda')
		delegate.(adminURI + "/agendaTrack/$action?")(controller: 'agendaTrack')

		println("TOOLS_PREFIX: $TOOLS_PREFIX")
		def toolFunctionsPrefix = (TOOLS_PREFIX ? '/' : '') + "${TOOLS_PREFIX}"

		name speakerImage: delegate.(toolFunctionsPrefix + "/speaker/img/$id") {
			controller = "speaker"
			action = 'img'
		}

		delegate.(toolFunctionsPrefix+"/agenda/summary") {
			controller = "agendaView"
			action = "summary"
		}

		"500"(view: '/error')
	}
}
