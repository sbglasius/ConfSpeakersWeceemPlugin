<html>
<head>
	<wcm:widget path="widgets/common-head"/>
	<wcm:agendaResources/>
</head>

<body>
<wcm:widget path="widgets/header"/>

<div class="container prepend-top">
	<div class="span-24 last" id="content">
		<wcm:agenda node="${node}"/>
	</div>
</div>

<div class="container prepend-top">
	<div class="span-24 last" id="content">
		<wcm:agendaLegend node="${node}"/>
	</div>
</div>

</body>
</html>
