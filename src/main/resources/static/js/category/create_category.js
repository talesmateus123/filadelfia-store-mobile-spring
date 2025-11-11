class CategoryManager {
    constructor() {
        this.form = document.getElementById('form-categoria');
        this.isEdit = !!document.getElementById('categoria-id').value;
        
        this.formManager = new FormManager(this.form, {
            onSubmit: (data) => this.handleFormSubmit(data)
        });
    }

    async handleFormSubmit(formData) {
        const submitOperation = this.isEdit ? 
            () => categoryService.update(formData.id, formData) :
            () => categoryService.create(formData);

        try {
            const result = await loadingManager.wrapAsync(submitOperation, {
                message: this.isEdit ? 'Atualizando categoria...' : 'Criando categoria...'
            })();

            notificationManager.success(
                `Categoria ${this.isEdit ? 'atualizada' : 'criada'} com sucesso!`
            );

            if (this.isEdit) {
                setTimeout(() => {
                    window.location.href = '/categories';
                }, 1500);
            }

            return result;
        } catch (error) {
            notificationManager.error(
                error.data?.message || `Erro ao ${this.isEdit ? 'atualizar' : 'criar'} categoria`
            );
            throw error;
        }
    }
}

document.addEventListener('DOMContentLoaded', function() {
    new CategoryManager();
});