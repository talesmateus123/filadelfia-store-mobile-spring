class ProductManager {
    constructor() {
        this.form = document.getElementById('form-produto');
        this.isEdit = !!document.getElementById('produto-id').value;
        
        this.formManager = new FormManager(this.form, {
            onSubmit: (data) => this.handleFormSubmit(data)
        });
    }

    async handleFormSubmit(formData) {
        const submitOperation = this.isEdit ? 
            () => productService.update(formData.id, formData) :
            () => productService.create(formData);

        try {
            const result = await loadingManager.wrapAsync(submitOperation, {
                message: this.isEdit ? 'Atualizando produto...' : 'Criando produto...'
            })();

            notificationManager.success(
                `Produto ${this.isEdit ? 'atualizado' : 'criado'} com sucesso!`
            );

            setTimeout(() => {
                window.onbeforeunload = null;
                window.location.href = '/products';
            }, 1000);

            return result;
        } catch (error) {
            notificationManager.error(
                error.data?.details && error.data?.details.join(", ") || error.data?.message || `Erro ao ${this.isEdit ? 'atualizar' : 'criar'} produto`
            );
            throw error;
        }
    }
}

document.addEventListener('DOMContentLoaded', function() {
    new ProductManager();
});