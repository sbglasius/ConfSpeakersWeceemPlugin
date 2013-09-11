package org.gr8conf.conference

import org.weceem.blog.WcmBlog
import org.weceem.content.WcmContent
import org.weceem.content.WcmTemplate

class Speakers  extends WcmContent {
    WcmTemplate template

	static icon = [plugin: "conf-speakers-weceem", dir: "images", file: "speakers.png"]

    static searchable = {
        alias WcmBlog.name.replaceAll("\\.", '_')
        only = ['title', 'status']
    }
    Map getVersioningProperties() {
        def r = super.getVersioningProperties() + [
            template:template?.ident() // Is this right?
        ]
        return r
    }

    static constraints = {
            template(nullable: true)
    }

    static mapping = {
        template cascade: 'all', lazy: false // we never want proxies for this
    }

    static transients = WcmContent.transients

    static editors = {
        template()
    }

}
