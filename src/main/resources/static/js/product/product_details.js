const deleteBtn = document.querySelector('#button_delete');
if (deleteBtn) {
    deleteBtn.addEventListener('click', (e) => {
        e.preventDefault();
        this.handleDeleteProduct();
    });
}

async function  handleDeleteProduct() {
    const productId = document.getElementById('productId')?.value;

    if (!productId) {
        notificationManager.error('ID do produto não encontrado');
        return;
    }

    modalManager.confirm({
        title: "Remover Produto",
        message: "Tem certeza que deseja remover este produto?",
        closeMessage: "Cancelar",
        confirmMessage: "Sim, Remover",
        onConfirm: () => {
            productService.delete(productId)
                .then(() => {
                    modalManager.success({
                        title: "Sucesso",
                        message: "Produto removido com sucesso!",
                        onClose: () => {
                            window.onbeforeunload = null;
                            window.location.href = '/products'
                        }
                    });
                })
                .catch(error => {
                    modalManager.error({
                        title: "Erro",
                        message: error.data?.message || "Erro ao remover Produto"
                    });
                });
        }
    });
}

async function executeDelete(categoryId) {
    try {
        await loadingManager.wrapAsync(
            () => productService.delete(productId),
            { message: 'Removendo produto...' }
        )();

        modalManager.show({
            title: "Sucesso",
            message: "Produto removido com sucesso.",
            type: "success",
            closeMessage: "Ok",
            onClose: () => {
                // Redireciona para a lista de produtos
                window.onbeforeunload = null;
                window.location.href = '/products';
            }
        });

    } catch (error) {
        modalManager.show({
            title: "Erro",
            message: error.data?.message || "Erro ao remover produto. Tente novamente.",
            type: "error",
            closeMessage: "Fechar"
        });
    }
}