package org.gr8conf.conference

import groovy.util.logging.Log4j
import org.springframework.beans.PropertyEditorRegistrar
import org.springframework.beans.PropertyEditorRegistry
import org.weceem.binding.DateAndTimeDateEditor
import java.text.SimpleDateFormat
import org.springframework.beans.propertyeditors.CustomDateEditor


@Log4j
class ConferencePropertyEditorRegistrar implements PropertyEditorRegistrar {

    public void registerCustomEditors(PropertyEditorRegistry registry) {
		log.info("Register custom editors for Conf Speaker plugin")
        registry.registerCustomEditor(Date, 'scheduled', new DateAndTimeDateEditor(new SimpleDateFormat("yyyy/MM/dd HH:mm"), true));
        registry.registerCustomEditor(Date, 'firstDay', new CustomDateEditor(new SimpleDateFormat("yyyy/MM/dd"), true));
        registry.registerCustomEditor(Date, 'lastDay', new CustomDateEditor(new SimpleDateFormat("yyyy/MM/dd"), true));
    }
}
