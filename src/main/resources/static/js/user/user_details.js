const deleteBtn = document.querySelector('#button_delete');
if (deleteBtn) {
    deleteBtn.addEventListener('click', (e) => {
        e.preventDefault();
        this.handleDeleteUser();
    });
}

async function  handleDeleteUser() {
    const userId = document.getElementById('userId')?.value;

    if (!userId) {
        notificationManager.error('ID do usuário não encontrado');
        return;
    }

    modalManager.confirm({
        title: "Remover Usuário",
        message: "Tem certeza que deseja remover este usuário?",
        closeMessage: "Cancelar",
        confirmMessage: "Sim, Remover",
        onConfirm: () => {
            userService.delete(userId)
                .then(() => {
                    modalManager.success({
                        title: "Sucesso",
                        message: "Usuário removido com sucesso!",
                        onClose: () => {
                            window.onbeforeunload = null;
                            window.location.href = '/users'
                        }
                    });
                })
                .catch(error => {
                    modalManager.error({
                        title: "Erro",
                        message: error.data?.message || "Erro ao remover Usuário"
                    });
                });
        }
    });
}

async function executeDelete(userId) {
    try {
        await loadingManager.wrapAsync(
            () => userService.delete(userId),
            { message: 'Removendo usuário...' }
        )();

        modalManager.show({
            title: "Sucesso",
            message: "Usuário removido com sucesso.",
            type: "success",
            closeMessage: "Ok",
            onClose: () => {
                // Redireciona para a lista de usuários
                window.onbeforeunload = null;
                window.location.href = '/users';
            }
        });

    } catch (error) {
        modalManager.show({
            title: "Erro",
            message: error.data?.message || "Erro ao remover usuário. Tente novamente.",
            type: "error",
            closeMessage: "Fechar"
        });
    }
}