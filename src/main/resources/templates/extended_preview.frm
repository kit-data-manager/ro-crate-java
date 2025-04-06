<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>${about.name!"Research Object Crate"}</title>
    <style>
        body { font-family: 'Segoe UI', sans-serif; margin: 0; padding: 0; background: #f0f2f5; }
        .header { background: #005a9c; color: white; padding: 30px 40px; }
        .header h1 { margin: 0; font-size: 28px; }
        .header .meta { margin-top: 10px; font-size: 16px; }
        .content { padding: 20px 40px; }
        .item { background: white; margin: 15px 0; padding: 15px 20px; border-radius: 10px; box-shadow: 0 1px 3px rgba(0,0,0,0.1); }
        .item h3 { margin: 0 0 10px 0; font-size: 18px; color: #333; }
        .item .field { margin: 3px 0; font-size: 14px; }
        .field strong { color: #555; }
    </style>
</head>
<body>
<#-- Find main entry with @type = CreativeWork and 'about' property -->
<#assign main = graph?filter(i -> i["@type"]?contains("CreativeWork") && i["about"]??)?first>
<#assign about = main.about>

<div class="header">
    <h1>${about.name! "Untitled Dataset"}</h1>
    <div class="meta">
        <div><strong>Date Published:</strong> ${about.datePublished! "N/A"}</div>
        <div><strong>License:</strong> ${about.license! "N/A"}</div>
        <div><strong>Based On:</strong> ${about.isBasedOn! "N/A"}</div>
    </div>
</div>

<div class="content">
    <h2>Contained Items</h2>

    <#-- Render hasPart section -->
    <#if about.hasPart??>
        <#list about.hasPart as part>
            <div class="item">
                <h3>${part.name! part["@id"]}</h3>
                <div class="field"><strong>Type:</strong> ${part["@type"]!}</div>
                <#if part.version??>
                    <div class="field"><strong>Version:</strong> ${part.version}</div>
                </#if>
                <#if part.url??>
                    <div class="field"><strong>URL:</strong> <a href="${part.url}">${part.url}</a></div>
                </#if>
                <#if part.creator??>
                    <div class="field"><strong>Creator:</strong>
                        <#list part.creator as c>
                            ${c.name! "Unknown"}<#if c_has_next>, </#if>
                        </#list>
                    </div>
                </#if>
                <#if part.programmingLanguage??>
                    <div class="field"><strong>Language:</strong> ${part.programmingLanguage.name!}</div>
                </#if>
            </div>
        </#list>
    </#if>

    <#-- Render mentions -->
    <#if about.mentions??>
        <h2>Mentions</h2>
        <#list about.mentions as m>
            <div class="item">
                <h3>${m.name! m["@id"]}</h3>
                <div class="field"><strong>Type:</strong> ${m["@type"]!}</div>
                <#if m.definition??>
                    <div class="field"><strong>Definition:</strong> ${m.definition["@id"]!}</div>
                </#if>
                <#if m.mainEntity??>
                    <div class="field"><strong>Main Entity:</strong> ${m.mainEntity["@id"]!}</div>
                </#if>
                <#if m.instance??>
                    <#list m.instance as i>
                        <div class="field"><strong>Instance:</strong> ${i.name!} (${i.url!})</div>
                        <#if i.runsOn??>
                            <div class="field"><strong>Runs On:</strong> ${i.runsOn.name!} - ${i.runsOn.url["@id"]!}</div>
                        </#if>
                    </#list>
                </#if>
            </div>
        </#list>
    </#if>
</div>
</body>
</html>
