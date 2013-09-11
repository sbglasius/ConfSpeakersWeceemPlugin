<html>
  <head>
    <wcm:widget path="widgets/common-head"/>
  </head>
  <body>
    <wcm:widget path="widgets/header"/>
    <div class="container prepend-top">
      <div class="span-24 last" id="content">
        <wcm:ifContentIs type="org.gr8conf.conference.Speakers">
          <wcm:eachChild type="org.gr8conf.conference.Speaker" var="child" sort="title" counter="i">
            <div class="span-8 ${i % 3 == 2 ? 'last':''} speaker-entry" style="padding-bottom: 10px">
              <wcm:widget model="[node:child]" path="widgets/speaker-header-widget"/>
            </div>
          </wcm:eachChild>
        </wcm:ifContentIs>

        <wcm:ifContentIs type="org.gr8conf.conference.Speaker">
          <div class="speaker-entry">
            <div class="span-24 last">
              <wcm:widget model="[node:node]" path="widgets/speaker-header-widget"/>
            </div>
            <div class="span-24 last" style="padding-top: 10px">
              <h1 class="loud">Biography:</h1>
              ${node.contentAsHTML}
            </div>
            <g:if test="${node.presentations}">
              <hr/>
              <div class="span-24 last">
                <h3 class="loud">Presents:</h3>
                <ul>
                  <g:each in="${node.presentations}" var="presentation">
                    <li><wcm:link node="${presentation}">${presentation.titleForHTML}</wcm:link></li>
                  </g:each>
                </ul>
              </div>
            </g:if>
          </div>
        </wcm:ifContentIs>
      </div>
    </div>
    <wcm:widget path="widgets/footer"/>
  </body>
</html>
