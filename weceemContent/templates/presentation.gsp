<html>
  <head>
    <wcm:widget path="widgets/common-head"/>
  </head>
  <body>
    <wcm:widget path="widgets/header"/>
    <div class="container prepend-top">
      <div class="span-24 last" id="content">
        <wcm:ifContentIs type="org.gr8conf.conference.Presentations">
          <wcm:eachChild type="org.gr8conf.conference.Presentation" var="child" sort="title" counter="i">
            <div class="span-8 ${i % 3 == 2 ? 'last':''} presentation-entry" style="padding-bottom: 10px">
              <wcm:link node="${child}">${child.title}</wcm:link>
            </div>
          </wcm:eachChild>
          <div class="span-24 last">
            <p>(*) University Sessions on June 6th.</p>
          </div>
        </wcm:ifContentIs>

        <wcm:ifContentIs type="org.gr8conf.conference.Presentation">
          <div class="presentation-entry">
            <div class="span-24 last">
              <wcm:widget model="[node:node]" path="widgets/presentation-header-widget"/>
            </div>
            <div class="span-24 last" style="padding-top: 10px">
              ${node.contentAsHTML}
            </div>
            <g:if test="${node.speakers}">
              <hr/>
              <div class="span-24 last">
                <h3 class="loud">Presented by:</h3>
                  <g:each in="${node.speakers}" var="speaker" status="i">
                    <div class="span-8 ${i % 3 == 2 ? 'last':''} speaker-entry" style="padding-bottom: 10px">
                      <wcm:widget model="[node:speaker]" path="widgets/speaker-header-widget"/>
                    </div>
                  </g:each>
              </div>
            </g:if>

          </div>
        </wcm:ifContentIs>
      </div>
    </div>
    <wcm:widget path="widgets/footer"/>
  </body>
</html>
