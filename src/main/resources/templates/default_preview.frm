<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${crate.name!"RO-Crate Metadata"}</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 20px; padding: 20px; background-color: #f4f4f4; }
        .container { max-width: 800px; margin: auto; background: #fff; padding: 20px; border-radius: 10px; box-shadow: 0 0 10px rgba(0,0,0,0.1); }
        h1 { color: #333; }
        h2 { border-bottom: 2px solid #ddd; padding-bottom: 5px; }
        .metadata, .tree { margin-bottom: 20px; }
        .metadata div, .tree div { margin: 5px 0; }
        .tree ul { list-style-type: none; padding-left: 20px; }
        .tree li { margin: 5px 0; }
        .tooltip { border-bottom: 1px dotted #000; cursor: help; }
    </style>
    <script>
        async function fetchSchemaTooltip(type) {
            const url = 'https://schema.org/' + type;
            try {
                const response = await fetch(url);
                const text = await response.text();
                const match = text.match(/<meta name="description" content="(.*?)"/i);
                return match ? match[1] : "No description available.";
            } catch (error) {
                return "Failed to fetch description.";
            }
        }

        document.addEventListener("DOMContentLoaded", () => {
            document.querySelectorAll(".tooltip").forEach(async element => {
                const type = element.dataset.type;
                if (type) {
                    const tooltipText = await fetchSchemaTooltip(type);
                    element.title = tooltipText;
                }
            });
        });
    </script>
</head>
<body>
    <div class="container">
        <h1>${name!"Untitled Dataset"}</h1>
        <h2>Metadata</h2>
        <div class="metadata">
            <#if crate.description??>
                <div><strong class="tooltip" data-type="description">Description:</strong> ${crate.description}</div>
            </#if>
            <#if crate.license??>
                <div><strong class="tooltip" data-type="license">License:</strong> <a href="${crate.license!}">${crate.license!}</a></div>
            </#if>
            <#if crate.datePublished??>
                <div><strong class="tooltip" data-type="datePublished">Published:</strong> ${crate.datePublished}</div>
            </#if>
        </div>

        <h2>Contained Files</h2>
        <div class="tree">
            <ul>
                <#list crate.hasPart as part>
                    <li class="tooltip" data-type="hasPart">${part['@id']}
                        <#list files as item>
                            <#if item['@id'] == part['@id']>
                                <ul>
                                    <#if item.name??>
                                        <li><strong class="tooltip" data-type="name">Name:</strong> ${item.name}</li>
                                    </#if>
                                    <#if item.description??>
                                        <li><strong class="tooltip" data-type="description">Description:</strong> ${item.description}</li>
                                    </#if>
                                    <#if item.contentSize??>
                                        <li><strong class="tooltip" data-type="contentSize">Size:</strong> ${item.contentSize}</li>
                                    </#if>
                                    <#if item.encodingFormat??>
                                        <li><strong class="tooltip" data-type="encodingFormat">Format:</strong> ${item.encodingFormat}</li>
                                    </#if>
                                </ul>
                            </#if>
                        </#list>
                        <#list datasets as item>
                            <#if item['@id'] == part['@id']>
                                <ul>
                                    <#if item.name??>
                                        <li><strong class="tooltip" data-type="name">Name:</strong> ${item.name}</li>
                                    </#if>
                                    <#if item.description??>
                                        <li><strong class="tooltip" data-type="description">Description:</strong> ${item.description}</li>
                                    </#if>
                                    <#if item.contentSize??>
                                        <li><strong class="tooltip" data-type="contentSize">Size:</strong> ${item.contentSize}</li>
                                    </#if>
                                    <#if item.encodingFormat??>
                                        <li><strong class="tooltip" data-type="encodingFormat">Format:</strong> ${item.encodingFormat}</li>
                                    </#if>
                                </ul>
                            </#if>
                        </#list>
                    </li>
                </#list>
            </ul>
        </div>
    </div>
</body>
</html>