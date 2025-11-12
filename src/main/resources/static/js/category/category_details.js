const deleteBtn = document.querySelector('#button_delete');
if (deleteBtn) {
    deleteBtn.addEventListener('click', (e) => {
        e.preventDefault();
        this.handleDeleteCategory();
    });
}

async function  handleDeleteCategory() {
    const categoryId = document.getElementById('categoryId')?.value;

    if (!categoryId) {
        notificationManager.error('ID da categoria não encontrado');
        return;
    }

    modalManager.confirm({
        title: "Remover Categoria",
        message: "Tem certeza que deseja remover esta categoria?",
        closeMessage: "Cancelar",
        confirmMessage: "Sim, Remover",
        onConfirm: () => {
            categoryService.delete(categoryId)
                .then(() => {
                    modalManager.success({
                        title: "Sucesso",
                        message: "Categoria removida com sucesso!",
                        onClose: () => {
                            window.onbeforeunload = null;
                            window.location.href = '/categories'
                        }
                    });
                })
                .catch(error => {
                    modalManager.error({
                        title: "Erro",
                        message: error.data?.message || "Erro ao remover categoria"
                    });
                });
        }
    });
}

async function executeDelete(categoryId) {
    try {
        await loadingManager.wrapAsync(
            () => categoryService.delete(categoryId),
            { message: 'Removendo categoria...' }
        )();

        modalManager.show({
            title: "Sucesso",
            message: "Categoria removida com sucesso.",
            type: "success",
            closeMessage: "Ok",
            onClose: () => {
                // Redireciona para a lista de categorias
                window.location.href = '/categories';
            }
        });

    } catch (error) {
        modalManager.show({
            title: "Erro",
            message: error.data?.message || "Erro ao remover categoria. Tente novamente.",
            type: "error",
            closeMessage: "Fechar"
        });
    }
}