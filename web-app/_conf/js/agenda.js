;
(function ($, window, document, undefined) {
    var pluginName = 'agenda',
            defaults = {
                trackListCacheId:undefined,
                trackListUrl:undefined,
                saveTracksUrl:undefined,
                saveTrackDataUrl:undefined,
                firstDay:undefined,
                lastDay:undefined,
                width:950,
                height:950,
                removeTo:undefined
            };

    var trackListCache = {};

    function AgendaPlugin(element, options) {

        this.element = element;
        this.$element = $(element);
        this.options = $.extend({}, defaults, options);
        this._defaults = defaults;

        this._name = pluginName;
        this.init();

    }

    function EventDragable(element, options) {
        this.element = element;
        this.$element = $(element);
        this.options = $.extend({}, defaults, options);
        this._name = pluginName;
        this.init();

    }

    EventDragable.prototype.init = function () {
        var element = this.$element;
        var agendaScheduleType = $('input#agendaScheduleType', element).remove().val();
        var presentation = $('input#presentation', element).remove().val();
        var title = element.text().trim();
        var eventObject = {
            title:title,
            agendaScheduleType:agendaScheduleType,
            presentation:presentation
        };
        this.$element.button()
                .data('eventObject', eventObject);
        console.debug(element)
        this.$element.draggable({
            zIndex:9999,
            revert:true,
            revertDuration:0,
            appendTo:'body',
            cursor:"move",
            helper:'clone',
            scope:'presentation',
            start:function (event, ui) {
                console.debug("Start drag")
                $(ui.helper.context).button('disable')
            },
            stop:function (event, ui) {
                console.debug("End drag")
                $(ui.helper.context).button('enable')
            }
        });

    };

    AgendaPlugin.prototype.init = function () {
        var opts = this.options,
                self = this,
                tracks = loadTracks.apply(self),
                firstDay = opts.firstDay,
                lastDay = opts.lastDay;

        var agenda = this.$element;
        agenda.fullCalendar({
            theme:true,
            header:{
                left:'',
                center:' ',
                right:'prev,next'
            },
            width:self.options.width,
            height:self.options.height,
            contentHeight:opts.height + 50,
            eventSources:tracks,
            allDayDefault:false,
            editable:true,
            defaultView:'agendaWeek',
            defaultEventMinutes:60,
            start:firstDay,
            end:lastDay,
            visStart:firstDay,
            visEnd:lastDay,
            allDaySlot:false,
            slotMinutes:30,
            minTime:'8:00',
            maxTime:'23:00',
            axisFormat:'H(:mm)',
            timeFormat:'H:mm',
            year:firstDay.getFullYear(),
            month:firstDay.getMonth(),
            date:firstDay.getDate(),
            droppable:true,
            viewDisplay:function (view) {
                var headerRight = $('.fc-header-right', agenda);
                var headerLeft = $('.fc-header-left', agenda);

                if(headerRight.data('saveButton')) return;
                headerRight.data('saveButton', true);
                $('<button/>').text('Save agenda')
                        .button()
                        .css('float', 'right')
                        .appendTo(headerRight)
                        .click(function () {
                            $(this).button('disable');
                            var processed = [];
                            var status = $('<div>Status: </div>').appendTo(headerLeft);
                            $.each(tracks, function (i, track) {
                                $.each(track.events, function (i, event) {
                                    var xstart = new XDate(event.start);
                                    var xend = new XDate(event.end || xstart.clone().addMinutes(60));
                                    var trackData = {
                                        id:event.id,
                                        agendaScheduleType:event.agendaScheduleType,
                                        start:xstart.toString('yyyy-MM-dd HH:mm'),
                                        duration:xstart.diffMinutes(xend)
                                    };
                                    if(event.agendaScheduleType == 'AgendaSchedule') {
                                        trackData.track = track.id;
                                        trackData.presentation = event.presentation
                                    } else if(event.agendaScheduleType == 'AgendaScheduleBreak') {
                                        trackData.title = event.title
                                    }
                                    if(event.changed) {
                                        status.text("Updating: " + event.title);
                                        $.ajax({
                                            url:opts.saveTrackDataUrl,
                                            async:false,
                                            data:{trackData:JSON.stringify(trackData)},
                                            success:function (data) {
                                                processed.push(data.id)
                                            }
                                        });
                                    } else {
                                        processed.push(event.id)
                                    }

                                });
                            });
                            $.ajax({
                                url:opts.removeObsoleteUrl,
                                async:false,
                                data:{currentIds:JSON.stringify(processed)},
                                success:function (data) {
                                }
                            });
                            status.remove()
                            location.reload();
                        });

            },
            dropAccept:function () {
                return $(this).data('eventObject')
            },
            drop:function (date, allDay) {
                console.debug("Drop", date)
                var event = $(this);
                var originalEventObject = event.data('eventObject');
                var newEvent = $.extend({}, originalEventObject);
                newEvent.start = date;
                newEvent.allDay = allDay;
                newEvent.changed = true;
                console.debug("New event: ", newEvent);
                if(newEvent.agendaScheduleType == 'AgendaSchedule') {
                    var defaultTrack = findDefaultTrack(tracks);
                    defaultTrack.events.push(newEvent);

                    agenda.fullCalendar('refetchEvents');
                    event.remove();
                } else if(newEvent.agendaScheduleType == 'AgendaScheduleBreak') {
                    var breakTrack = findBreakTrack(tracks);
                    newEvent.end = new XDate(date).addMinutes(30);
                    breakTrack.events.push(newEvent);
                    agenda.fullCalendar('refetchEvents');
                }
            },
            eventDrop:function (event, element) {
                event.changed = true
            },
            eventResize:function (event, element) {
                event.changed = true
            },

            eventRender:function (event, element) {
                element.data('eventObject', event);
                var header = $('.fc-event-head', element);
                var xstart = new XDate(event.start);
                var xend = new XDate(event.end || xstart.clone().addMinutes(60));
                var duration = xstart.diffMinutes(xend);
                if(event.changed) {
                    $('<span/>').text('* ').prependTo($('.fc-event-time', header));
                }
                $('<span/>').text(' (+' + duration + ')').appendTo($('.fc-event-time', header));
            },
            eventClick:function (event, jsEvent, view) {
                console.debug("event: ", event);
                var xstart = new XDate(event.start);
                var xend = new XDate(event.end || xstart.clone().addMinutes(60));

                var startFormatted = xstart.toString('HH:mm');
                var duration = xstart.diffMinutes(xend);

                var inputTitle = $('<input type="text" name="title" value="' + event.title + '">');
                var divTitle = $('<div/>').append('<label for="title">Title: </label>').append(inputTitle);
                var inputTime = $('<input type="text" name="time" value="' + startFormatted + '">');
                var divTime = $('<div/>').append('<label for="time">Start: </label>').append(inputTime);

                var inputDuration = $('<input type="number" name="duration" value="' + duration + '">');
                var divDuration = $('<div/>').append('<label for="duration">Duration: </label>').append(inputDuration);

                var tracks = loadTracks.apply(self);

                var selectTrack = $('<select name="track"/>');
                for(var i = 0; i < tracks.length; i++) {
                    var track = tracks[i];
                    if(track.id) {
                        var option = $('<option value="' + track.id + '">' + track.name + '</option>').appendTo(selectTrack);
                        if(track.id == event.source.id) {
                            option.attr('selected', 'selected');
                        }
                    }
                }
                var divTrack = $('<div/>').append('<label for="track">Track:</label>').append(selectTrack);

                var div = $('<div/>').addClass('event-edit');
                if(event.agendaScheduleType == 'AgendaScheduleBreak') {
                    div.append(divTitle);
                }

                div.append(divTime).append(divDuration);
                if(event.agendaScheduleType == 'AgendaSchedule') {
                    div.append(divTrack);
                }
                div.appendTo('body').dialog({
                    title:"Edit: " + event.title,
                    modal:true,
                    width:400,
                    buttons:{
                        'Ok':function () {
                            var time = inputTime.val().split(/:/);
                            xstart.setHours(time[0] || xstart.getHours());
                            xstart.setMinutes(time[1] || 0);
                            var dur = parseInt(inputDuration.val());
                            xend = xstart.clone().addMinutes(dur || duration);
                            if(event.agendaScheduleType == 'AgendaScheduleBreak') {
                                event.title = inputTitle.val()
                            }
                            if(event.agendaScheduleType == 'AgendaSchedule') {
                                var newTrackId = selectTrack.val();
                                if(newTrackId != event.source.id) {
                                    moveToTrack(event, tracks, newTrackId);
                                    agenda.fullCalendar('refetchEvents')
                                }
                            }
                            event.start = xstart.toDate();
                            event.end = xend.toDate();
                            event.changed = true;
                            agenda.fullCalendar('refetchEvents');
                            div.dialog('close');
                        },
                        'Remove':function () {
                            event.source.removeEvent(event);
                            if(opts.removeTo) {
                                opts.removeTo.apply(agenda, [event]);
                            }
                            agenda.fullCalendar('refetchEvents');
                            div.dialog('close');

                        },
                        'Cancel':function () {
                            div.dialog('close');
                        }
                    },
                    close:function () {
                        div.remove();
                    }
                });


                return false;
            }
        });
    }
    ;
    function loadTracks() {
        var cacheId = this.options.trackListCacheId || 'id_' + new Date().getTime();
        if(trackListCache[cacheId]) {
            return trackListCache[cacheId]
        }
        var tracks = [];
        $.ajax({
            url:this.options.trackListUrl,
            async:false,
            success:function (data) {
                tracks = data;
            }
        });
        $.each(tracks, function (i, track) {
            track.addEvent = function (event) {
                this.events.push(event);
                event.source = this;
            };
            track.removeEvent = function (event) {
                var idx = this.events.indexOf(event);
                if(idx != -1) {
                    this.events.splice(idx, 1);
                    event.source = undefined;
                }
            }
        });
        trackListCache[cacheId] = tracks;
        return tracks;
    }


    function findDefaultTrack(tracks) {
        for(var i = 0; i < tracks.length; i++) {
            console.debug(tracks[i]);
            if(tracks[i].id) {
                return tracks[i]
            }
        }
        return undefined
    }

    function findBreakTrack(tracks) {
        for(var i = 0; i < tracks.length; i++) {
            console.debug(tracks[i]);
            if(!tracks[i].id) {
                return tracks[i]
            }
        }
        return undefined
    }

    function findTrack(tracks, newTrackId) {
        var newTrack;
        for(var i = 0; i < tracks.length; i++) {
            if(tracks[i].id == newTrackId) {
                newTrack = tracks[i];
                break;
            }
        }
        return newTrack;
    }

    function moveToTrack(event, tracks, newTrackId) {
        event.source.removeEvent(event);
        var newTrack = findTrack(tracks, newTrackId);
        newTrack.addEvent(event);
    }

    $.fn.agenda = function (options) {
        return this.each(function () {
            if(!$.data(this, 'plugin_' + pluginName)) {
                $.data(this, 'plugin_' + pluginName, new AgendaPlugin(this, options));
            }
        });
    };

    $.fn.eventDragable = function (options) {
        return this.each(function () {
            if(!$.data(this, 'plugin_eventDragable')) {
                $.data(this, 'plugin_eventDragable', new EventDragable(this, options));
            }
        })
    }


})(jQuery, window, document);
		
