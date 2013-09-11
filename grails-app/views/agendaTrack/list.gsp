<%--suppress ALL --%>

<%@ page import="org.gr8conf.conference.AgendaTrack" %>
<link rel="stylesheet" href="${g.resource(plugin: 'conf-speakers-weceem', dir: '_conf/js/colorpicker/css', file: 'colorpicker.css')}"/>
<script type="text/javascript" src="${g.resource(plugin: 'conf-speakers-weceem', dir: '_conf/js/colorpicker/js', file: 'colorpicker.js')}"></script>
<script type="text/javascript" src="${g.resource(plugin: 'conf-speakers-weceem', dir: '_conf/js/colorpicker/js', file: 'eye.js')}"></script>
<script type="text/javascript" src="${g.resource(plugin: 'conf-speakers-weceem', dir: '_conf/js/colorpicker/js', file: 'utils.js')}"></script>
<script type="text/javascript" src="${g.resource(plugin: 'conf-speakers-weceem', dir: '_conf/js/colorpicker/js', file: 'layout.js')}"></script>
<style type="text/css">
.colorpicker {
	z-index: 9999;
}


</style>

<span id="title">Tracks for ${agendaInstance.title}</span>
<g:if test="${flash.message}">
	<div class="message" role="status">${flash.message}</div>
</g:if>
<g:form name="editTracks" action="update">
	<g:hiddenField name="agenda" value="${agendaInstance.id}"/>

	<table>
		<thead>
			<tr>
				<th>${message(code: 'agendaTrack.name.label', default: 'Name')}</th>
				<th>${message(code: 'agendaTrack.room.label', default: 'Room')}</th>
				<th>${message(code: 'agendaTrack.textColor.label', default: 'Text color')}</th>
				<th>${message(code: 'agendaTrack.backgroundColor.label', default: 'Background color')}</th>
				<th style="width: 16px;"></th>
			</tr>
		</thead>
		<tbody>
			<g:each in="${agendaInstance.tracks.sort {it.name}}" status="i" var="agendaTrack">
				<tr>
					<g:hiddenField name="id" value="${agendaTrack.id}"/>
					<g:hiddenField name="removed" value=""/>
					<td>
						<g:textField name="name" value="${agendaTrack.name}"/>
					</td>
					<td><g:textField name="room" value="${agendaTrack.room}"/></td>
					<td><g:textField name="textColor" value="${agendaTrack.textColor}" class="color"/></td>
					<td><g:textField name="backgroundColor" value="${agendaTrack.backgroundColor}" class="color"/></td>
					<td>
						<g:unless test="${agendaTrack.isInUse}">
							<a href="#" class="removeTrack"></a>
						</g:unless>
					</td>
				</tr>
			</g:each>
		</tbody>
		<tfoot>
			<tr>
				<td colspan="4">&nbsp;</td>
				<td><a href="#" id="addTrack"><span class="ui-icon ui-icon-circle-plus"></span></a></td>
			</tr>
		</tfoot>
	</table>
</g:form>
<g:javascript>
	$(function () {
		var form = $('form#editTracks');
		var tbody = $('tbody', form);
		var addTrack = $('a#addTrack', form);


		addTrack.click(function (event) {
			event.preventDefault();
			var tr = $('<tr>').appendTo(tbody);
			tr.append(createInput('hidden', 'id').val(''));
			tr.append(createInput('hidden', 'removed').val('false'));
			tr.append(createInput('text', 'name', true));
			tr.append(createInput('text', 'room', true));
			var textColor = createInput('text', 'textColor', true).addClass('color').val('#000000')
			tr.append(textColor);
			var backgroundColor = createInput('text', 'backgroundColor', true).addClass('color')
			tr.append(backgroundColor);
			var remove = $('<a class="removeTrack"/>')
			tr.append(remove.wrap('<td/>').parent());
			adjustMargin();
			createRemoveIcon(remove);

		});

		function createInput(type, name, wrap) {
			var input = $('<input/>').attr('type', type).attr('name', name);
			if(wrap) {
				input = input.wrap('<td/>').parent()
			}
			return input
		}

		function createRemoveIcon(remove) {
			console.debug($(remove));
			$('<span>').addClass('ui-icon ui-icon-trash').appendTo(remove);
		}

		function adjustMargin() {
			$('input[name="name"],input[name="room"]', form).css('margin', 0)
		}

		form.delegate('a.removeTrack', 'click', function (event) {
			var row = $(this).closest('tr');
			var id = $('input[name="id"]', row).val();
			console.debug(row, id)
			if(id) {
				$('input[name="removed"]', row).val('true');
			}
			row.slideUp('fast', function () {
				if(!id) {
					row.remove();
				}
			});

		});

		function HexToRGB(hex) {
			var int = parseInt(((hex.indexOf('#') > -1) ? hex.substring(1) : hex), 16);
			return {r:int >> 16, g:(int & 0x00FF00) >> 8, b:(int & 0x0000FF)};
		}

		function blackOrWhite(rgb) {
			return rgb.r + rgb.g + rgb.b > 400 ? 'black' : 'white'
		}

		function bindColorPicker(elements) {
			elements.attr('readonly', 'readonly').each(
					function (i, element) {
						var color = $(element).val();
						if(color) {
							$(element).css('color', blackOrWhite(HexToRGB(color)));
							$(element).css('backgroundColor', color);
						}
					}).ColorPicker({
						onSubmit:function (hsb, hex, rgb, element) {
							$(element).val('#' + hex);
							$(element).css('color', blackOrWhite(rgb));
							$(element).css('backgroundColor', '#' + hex);
							$(element).ColorPickerHide();
						},
						onBeforeShow:function () {
							$(this).ColorPickerSetColor(this.value);
						}
					})
		}

		form.bind('save', function (event, dialog) {
			var url = form.attr('action');
			var params = form.serializeArray();
			console.debug($(dialog), form, url, params);
			$.post(url, params, function (data) {
				if(data.ok) {
					$('#currentTracks').html(data.trackList);
					$('input[name="version"]').val(data.version);
					$(dialog).dialog('close');
				}
			})
		});

		adjustMargin();
		createRemoveIcon('.removeTrack', form);
		bindColorPicker($('.color', form));

	})

</g:javascript>
