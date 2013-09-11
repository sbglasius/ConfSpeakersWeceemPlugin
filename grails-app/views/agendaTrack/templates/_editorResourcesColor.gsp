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

<g:javascript type="text/javascript">
	$(function () {
		function HexToRGB(hex) {
			var int = parseInt(((hex.indexOf('#') > -1) ? hex.substring(1) : hex), 16);
			return {r:int >> 16, g:(int & 0x00FF00) >> 8, b:(int & 0x0000FF)};
		}

		function blackOrWhite(rgb) {
			return rgb.r + rgb.g + rgb.b > 400 ? 'black' : 'white'
		}

		$('.color').attr('readonly', 'readonly').each(
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
	});
</g:javascript>
