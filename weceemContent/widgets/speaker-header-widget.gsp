<div style="float: left; padding-right: 5px;">
  <wcm:speakerImg node="${node}" class="roundedCorners" style="width: 105px; height: 120px"/>
</div>
<h1 class="loud"><wcm:link node="${node}">${node.title.encodeAsHTML()}</wcm:link></h1>
<g:if test="${node.company}">
  <p>
    <g:if test="${node.website_url}">
      <a href="${node.website_url}" target="_blank">${node.company.encodeAsHTML()}</a>
    </g:if>
    <g:else>
      ${node.company.encodeAsHTML()}
    </g:else>
  </p>
</g:if>
