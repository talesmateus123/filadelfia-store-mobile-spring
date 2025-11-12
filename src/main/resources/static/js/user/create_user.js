class UserManager {
    constructor() {
        this.form = document.getElementById('form-user');
        this.isEdit = !!document.getElementById('user-id').value;
        
        this.formManager = new FormManager(this.form, {
            onSubmit: (data) => this.handleFormSubmit(data)
        });
    }

    async handleFormSubmit(formData) {
        const submitOperation = this.isEdit ? 
            () => userService.update(formData.id, formData) :
            () => userService.create(formData);

        try {
            const result = await loadingManager.wrapAsync(submitOperation, {
                message: this.isEdit ? 'Atualizando usuário...' : 'Criando usuário...'
            })();

            notificationManager.success(
                `Usuário ${this.isEdit ? 'atualizado' : 'criado'} com sucesso!`
            );

            setTimeout(() => {
                window.onbeforeunload = null;
                window.location.href = '/users';
            }, 1000);

            return result;
        } catch (error) {
            notificationManager.error(
                error.data?.details && error.data?.details.join(", ") || error.data?.message || `Erro ao ${this.isEdit ? 'atualizar' : 'criar'} usuário`
            );
            throw error;
        }
    }
}

document.addEventListener('DOMContentLoaded', function() {
    new UserManager();
});