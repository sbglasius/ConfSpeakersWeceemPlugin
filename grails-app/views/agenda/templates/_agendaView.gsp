<%@ page import="org.gr8conf.conference.DateUtils; grails.converters.JSON" %>
<div id="tabs">
	<ul>
		<g:each in="${agenda}" var="day" status="dayCount">
			<li><a href="#${elementId}_${dayCount}"><g:formatDate date="${day.day}" format="EEEE, MMMM dd" locale="${Locale.US}"/></a>
			</li>
		</g:each>
	</ul>

	<g:each in="${agenda}" var="day" status="dayCount">
		<div id="${elementId}_${dayCount}">
			<div class="calendar" style="height:${day.height}">
				<div class="calendar-hours">
					<g:each in="${day.agendaHours}" var="hour">
						<div class="calendar-hour" style="top: ${hour.top - 8}px; ">
							${hour.text}
						</div>
					</g:each>
				</div>
				<g:each in="${day.blocks}" var="block">
					<div class="calendar-block" style="top: ${block.top}px; height: ${block.bottom - block.top}px; margin-top: ${day.offset}px">
						<g:each in="${block.tracks}" var="track">
							<div class="calendar-track" style="width: ${track.width}; ">
								<g:each in="${track.schedules}" var="schedule">
									<div class="calendar-schedule" data-space="${space}" data-uri="${schedule.uri}" data-href="${g.createLink(uri: '/content' + schedule.presentation.uri)}" style="top: ${schedule.top}px; max-height: ${schedule.bottom - schedule.top}px; height: ${schedule.bottom - schedule.top}px; color: ${track.textColor};background-color: ${track.backgroundColor};">
										<div class="calendar-schedule-box" ng-click="selectSchedule(schedule.presentation.name)">
											<div class="calendar-schedule-time">${schedule.displayTime}:</div>

											<div class="calendar-schedule-info ui-corner-all" style="background-color: white"><span class="ui-icon ui-icon-info info" style="color: ${track.textColor}"></span>%{--<span class="ui-icon ui-icon-star star" ></span>--}%
											</div>

											<div class="calendar-schedule-text">${schedule.title}</div>
										</div>
									</div>
								</g:each>
							</div>
						</g:each>
					</div>
				</g:each>
				<div class="calendar-breaks">
					<g:each in="${day.breaks}" var="brk">
						<div class="calendar-break" style="top: ${brk.top + day.offset}px; height: ${brk.bottom - brk.top}px;">
							${brk.title}
						</div>
					</g:each>
				</div>
			</div>
		</div>
	</g:each>
</div>
<g:javascript>
	$(function () {
		$("#tabs").tabs({
			cookie: {
				expires: 1
			}
		});
		$('.calendar-schedule').click(function () {
			location.href = $(this).attr('data-href');
		});

		$('.info').click(function (event) {
			event.stopPropagation();
		}).qtip(
				{
					content: {
						// Set the text to an image HTML string with the correct src URL to the loading image you want to use
						text: 'Loading...',
						title: {
							text: 'Loading...', // Give the tooltip a title using each elements text
							button: true
						}
					},
					position: {
						at: 'bottom center', // Position the tooltip above the link
						my: 'bottom center',
						viewport: $(window), // Keep the tooltip on-screen at all times
						effect: false // Disable positioning animation
					},
					show: {
						event: 'click',
						solo: true // Only show one tooltip at a time
					},
					hide: 'unfocus',
					style: {
								classes: 'ui-tooltip-light ui-tooltip-shadow ui-tooltip-rounded'
					},
					events: {
							show: function(event, api) {
								var schedule = $(api.elements.target).closest('.calendar-schedule');
								var title = $('.calendar-schedule-text',schedule).text();
								var uri = schedule.attr('data-uri');
								api.set('content.title.text',title);
								$.ajax({
									url: '${createLink(controller: 'agendaView', action: 'summary')}',
									data: { uri: uri },
									success: function(data) {
										api.set('content.text', data.content);
									},
									crossDomain: true
								});

								}
							}
				})
	});
</g:javascript>
