<div>
	<g:if test="${content.id}">
		<g:link elementId="${property}" controller="agendaTrack" action="list" id="${content.id}" params="[space: content.space.id]">Edit tracks</g:link>
		<div id="currentTracks">
			<g:render plugin="conf-speakers-weceem" template="/agendaTrack/templates/currentTracks"/>
		</div>
		<g:javascript>
			$(function() {
				$('#${property.encodeAsJavaScript()}').button().click(function(event) {
						event.preventDefault();
						var url = $(this).attr('href');
						$('<div/>').appendTo('body').load(url, function() {
							var dialog = $(this);
							var title = dialog.find('span#title').remove().text();
							dialog.dialog({
								title: title,
								modal: true,
								width: 700,
								buttons: {
									"Cancel": function() {
										dialog.dialog('close');
									},
									"Save":function() {
										dialog.find('form').trigger('save', [this]);
									}
								},
								close: function() {
									dialog.dialog('destroy');
									dialog.remove();
								}
							});
						});
					});
			});
		</g:javascript>
	</g:if>
	<g:else>
		<p style="margin-top: 7px;margin-bottom: 4px;">Save agenda to add tracks</p>
	</g:else>
</div>
