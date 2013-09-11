<div>
	<g:if test="${!content.id}">
		<p style="margin-top: 7px;margin-bottom: 4px;">Save to edit the agenda</p>
	</g:if>
	<g:elseif test="${!content.children}">
		<p style="margin-top: 7px;margin-bottom: 4px;">Add agenda tracks, before you can layout the agenda</p>
	</g:elseif>
	<g:else>
		<g:link elementId="${property}" controller='agenda' action="edit" id="${content.id}">Edit layout</g:link> (Save before you layout agenda)
		<g:javascript>
			$(function() {
				$('#${property.encodeAsJavaScript()}').button()
			})
		</g:javascript>
	</g:else>
</div>
