<div class="speaker">
    <div class="name"><h3>${fieldValue(bean: speakerInstance, field: "name")}</h3></div>

    <div class="company"><a href="${speakerInstance.web}" target="_blank">${speakerInstance.company}</a></div>

    <div class="bio"><h4>Biography:</h4>
        ${speakerInstance.bio}
    </div>
    <g:if test="${speakerInstance.presentations.size()}">
        <div class="presentations">
            <h4>Will do ${speakerInstance.presentations.size() == 1 ? 'this presentation' : 'these presentations'}:</h4>
            <ul>
                <g:each var="presentation" in="${speakerInstance.presentations}">
                    <li>
                        {p.title}
                        %{--<link:talk name="${presentation.name}">${presentation.title}</link:talk>--}%
                    </li>
                </g:each>
            </ul>
        </div>
    </g:if>
    <g:else>
        <h4>Speakers presentation(s) will be posted here shortly.</h4>
    </g:else>
</div>
