class CloudinaryUploadAdapter {
    constructor(loader) {
        this.loader = loader;
        this.cloudName = 'dodna125k';        
        this.uploadPreset = 'unsigned_preset';   
    }

    upload() {
        return this.loader.file
            .then(file => new Promise((resolve, reject) => {
                const data = new FormData();
                data.append('file', file);
                data.append('upload_preset', this.uploadPreset);

                fetch(`https://api.cloudinary.com/v1_1/${this.cloudName}/image/upload`, {
                    method: 'POST',
                    body: data
                })
                    .then(res => res.json())
                    .then(result => {
                        resolve({ default: result.secure_url });
                    })
                    .catch(reject);
            }));
    }

    abort() {}
}

function MyCustomUploadAdapterPlugin(editor) {
    editor.plugins.get('FileRepository').createUploadAdapter = (loader) => {
        return new CloudinaryUploadAdapter(loader);
    };
}

window.MyCustomUploadAdapterPlugin = MyCustomUploadAdapterPlugin;
