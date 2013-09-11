package org.gr8conf.conference

import grails.test.mixin.*

@TestFor(AgendaTrackController)
@Mock(AgendaTrack)
class AgendaTrackControllerTests {


	def populateValidParams(params) {
		assert params != null
		// TODO: Populate valid properties like...
		//params["name"] = 'someValidName'
	}


	void testIndex() {
		controller.index()
		assert "/agendaTrack/list" == response.redirectedUrl
	}


	void testList() {
		def model = controller.list()

		assert model.agendaTrackInstanceList.size() == 0
		assert model.agendaTrackInstanceTotal == 0
	}


	void testCreate() {
		def model = controller.create()

		assert model.agendaTrackInstance != null
	}


	void testSave() {
		controller.save()

		assert model.agendaTrackInstance != null
		assert view == '/agendaTrack/create'

		response.reset()

		populateValidParams(params)
		controller.save()

		assert response.redirectedUrl == '/agendaTrack/show/1'
		assert controller.flash.message != null
		assert AgendaTrack.count() == 1
	}


	void testShow() {
		controller.show()

		assert flash.message != null
		assert response.redirectedUrl == '/agendaTrack/list'


		populateValidParams(params)
		def agendaTrack = new AgendaTrack(params)

		assert agendaTrack.save() != null

		params.id = agendaTrack.id

		def model = controller.show()

		assert model.agendaTrackInstance == agendaTrack
	}


	void testEdit() {
		controller.edit()

		assert flash.message != null
		assert response.redirectedUrl == '/agendaTrack/list'


		populateValidParams(params)
		def agendaTrack = new AgendaTrack(params)

		assert agendaTrack.save() != null

		params.id = agendaTrack.id

		def model = controller.edit()

		assert model.agendaTrackInstance == agendaTrack
	}


	void testUpdate() {
		controller.update()

		assert flash.message != null
		assert response.redirectedUrl == '/agendaTrack/list'

		response.reset()


		populateValidParams(params)
		def agendaTrack = new AgendaTrack(params)

		assert agendaTrack.save() != null

		// test invalid parameters in update
		params.id = agendaTrack.id
		//TODO: add invalid values to params object
		controller.update()

		assert view == "/agendaTrack/edit"
		assert model.agendaTrackInstance != null

		agendaTrack.clearErrors()

		populateValidParams(params)
		controller.update()

		assert response.redirectedUrl == "/agendaTrack/show/$agendaTrack.id"
		assert flash.message != null

		//test outdated version number
		response.reset()
		agendaTrack.clearErrors()

		populateValidParams(params)
		params.id = agendaTrack.id
		params.version = -1
		controller.update()

		assert view == "/agendaTrack/edit"
		assert model.agendaTrackInstance != null
		assert model.agendaTrackInstance.errors.getFieldError('version')
		assert flash.message != null
	}


	void testDelete() {
		controller.delete()
		assert flash.message != null
		assert response.redirectedUrl == '/agendaTrack/list'

		response.reset()

		populateValidParams(params)
		def agendaTrack = new AgendaTrack(params)

		assert agendaTrack.save() != null
		assert AgendaTrack.count() == 1

		params.id = agendaTrack.id

		controller.delete()

		assert AgendaTrack.count() == 0
		assert AgendaTrack.get(agendaTrack.id) == null
		assert response.redirectedUrl == '/agendaTrack/list'
	}
}
