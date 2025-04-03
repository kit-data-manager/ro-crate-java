@import edu.kit.datamanager.ro_crate.preview.DefaultPreviewGenerator.ROCrateModel
@import edu.kit.datamanager.ro_crate.preview.DefaultPreviewGenerator.Part
@import edu.kit.datamanager.ro_crate.preview.DefaultPreviewGenerator.Dataset
@import edu.kit.datamanager.ro_crate.preview.DefaultPreviewGenerator.File

@param ROCrateModel model

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>RO-Crate Viewer</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body>
    <div class="container mt-5">
        <h1 class="mb-4">RO-Crate Metadata</h1>
        <div class="card p-4">
            <h2>${model.crate.name}</h2>
            <p><strong>Description:</strong> ${model.crate.description}</p>
            <p><strong>Type:</strong> ${model.crate.type}</p>
            <p><strong>License:</strong> <a href="${model.crate.license}" target="_blank">License</a></p>
            <p><strong>Date Published:</strong> ${model.crate.datePublished}</p>
            <h3>Contains:</h3>
            <ul>
                @for(Part part : model.crate.hasPart) {
                    <li><a href="#${part.id}">${part.name}</a></li>
                @endfor}
            </ul>
        </div>
        
        @for(Dataset dataset : model.datasets) {
        <div class="card p-4 mt-4" id="${dataset.id}">
            <h2>Dataset: ${dataset.name}</h2>
            <p><strong>Description:</strong> ${dataset.description}</p>
        </div>
        @endfor}
        
        @if(!model.files.isEmpty()){
        @for(File file : model.files) {
        <div class="card p-4 mt-4" id="${file.id}">
            <h2>File: ${file.name}</h2>
            <p><strong>Description:</strong> ${file.description}</p>
            <p><strong>Size:</strong> ${file.contentSize} bytes</p>
            <p><strong>Format:</strong> ${file.encodingFormat}</p>
        </div>
        @endfor}
    }@else <h2>No files.</h2>
    @endif
    </div>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
