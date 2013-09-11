<%@ page import="org.gr8conf.conference.AgendaTrack" %>
<g:each in="${node.agendaTracks.sort {it?.orderIndex}}">
	<div style="margin-bottom: 6px">
		<div style="width: 400px; height: 40px;" class="ui-widget-content ui-corner-all">
			<div style="color: ${it.textColor}; background: ${it.backgroundColor};width: 400px;padding: 2px;" class="ui-corner-top">
				<g:if test="${it.room}">Room: ${it.room}</g:if>
		  </div>
			<div style="color: ${it.textColor}; background: ${it.backgroundColor};width: 400px;padding: 2px;opacity: 0.9;" class="ui-corner-bottom">${it.title}</div>
		</div>
	</div>
</g:each>
