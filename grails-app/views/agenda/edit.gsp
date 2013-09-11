<%@ page import="org.gr8conf.conference.DateUtils" %>
<html>
<head>
	<meta name="layout" content="${wcm.adminLayout().toString()}"/>
	<title><g:message code="agenda.title.edit" default="Layout"/> <g:message code="${'content.type.name.' + wcm.getClassName(node: agenda)}" encodeAs="HTML"/> - ${agenda.title.encodeAsHTML()}</title>
	<g:render template="templates/agendaHeaders"/>
    <script type="text/javascript" src="${g.resource(plugin: 'conf-speakers-weceem', dir: '_conf/js/fullcalendar', file: 'fullcalendar.js')}"></script>
	<link rel="stylesheet" href="${g.resource(plugin: 'conf-speakers-weceem', dir: '_conf/js/fullcalendar', file: 'fullcalendar.css')}"/>
	<link rel="stylesheet" href="${g.resource(plugin: 'conf-speakers-weceem', dir: '_conf/css/', file: 'agenda.css')}"/>
	<script type="text/javascript" src="${g.resource(plugin: 'conf-speakers-weceem', dir: '_conf/js/xdate', file: 'xdate.js')}"></script>
	<script type="text/javascript" src="${g.resource(plugin: 'conf-speakers-weceem', dir: '_conf/js', file: 'agenda.js')}"></script>
	<script type="text/javascript" src="${g.resource(plugin: 'conf-speakers-weceem', dir: '_conf/js', file: 'unscheduled.js')}"></script>
</head>

<body>

<div class="container">
	<div class="span-24 last">
		<h1><g:message code="agenda.title.edit" default="Layout"/> <g:message code="${'content.type.name.' + wcm.getClassName(node: agenda)}" encodeAs="HTML"/></h1>
		<h4>From <g:formatDate date="${agenda.firstDay}" format="yyyy-MM-dd"/> to <g:formatDate date="${agenda.lastDay}" format="yyyy-MM-dd"/></h4>

		<div class="agenda">
		</div>

		<div id="unscheduledPresentations" style="display: none;">
			<div class="presentation">
				Break
				<g:hiddenField name="agendaScheduleType" value="AgendaScheduleBreak"/>
			</div>
			<g:each in="${unscheduledPresentations}" var="presentation">
				<div class="presentation" id="${presentation.id}">
					${presentation.title}
					<g:hiddenField name="agendaScheduleType" value="AgendaSchedule"/>
					<g:hiddenField name="presentation" value="${presentation.id}"/>
				</div>
			</g:each>
		</div>
	</div>
</div>

<g:javascript>
  var firstDay = $.fullCalendar.parseDate("${DateUtils.iso8601Format(agenda.firstDay)}", true);
  var lastDay = $.fullCalendar.parseDate("${DateUtils.iso8601Format(new Date(agenda.lastDay.time+2))}", true);

	$('.agenda').agenda({
		trackListCacheId: 'cache',
		trackListUrl: '${g.createLink(action: "trackList", id: agenda.id)}',
		saveTrackDataUrl: '${g.createLink(action: "saveTrackData", id: agenda.id)}',
		removeObsoleteUrl: '${g.createLink(action: 'removeObsoleteSchedules', id: agenda.id)}',
		firstDay: firstDay,
		lastDay: lastDay,
		removeTo: function(event) {
			$('<div/>').addClass('presentation')
			.text(event.title)
			.eventDragable()
			.appendTo('#unscheduledPresentations');
		}
	});
	$('#unscheduledPresentations').unscheduled();

</g:javascript>
</body>
</html>
