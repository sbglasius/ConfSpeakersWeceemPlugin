<g:if test="${schedule.presentation.summary}">
<div>${schedule.presentation.content}</div>
</g:if>
<g:if test="${schedule.presentation.speakers}">
<h5>Presented by:</h5>
<div>
	<g:join in="${schedule.presentation.speakers*.title}"/>
</div>
</g:if>
<h5>In ${schedule.parent.room}</h5>
