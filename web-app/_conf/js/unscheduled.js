function initializeUnscheduledPresentations() {
}
;
(function ($, window, document, undefined) {
    var pluginName = 'unscheduled',
            defaults = {};

    function Plugin(element, options) {
        this.element = element;
        this.$element = $(element);
        this.options = $.extend({}, defaults, options);

        this._defaults = defaults;
        this._name = pluginName;

        this.init();
    }

    Plugin.prototype.init = function () {
        this.$element.dialog({
            title:'Unscheduled presentations',
            closeOnEscape:false,
            open:function (event, ui) {
                var self = $(this);
                self.css('overflow', 'none');
                $(".ui-dialog-titlebar-close", self.closest('.ui-dialog')).hide();
                $('.presentation', self).eventDragable();
            },
            position:['left', 'bottom']
        })
    };

    $.fn.unscheduled = function (options) {
        return this.each(function () {
            if(!$.data(this, 'plugin_' + pluginName)) {
                $.data(this, 'plugin_' + pluginName, new Plugin(this, options));
            }
        });
    }
})(jQuery, window, document);
