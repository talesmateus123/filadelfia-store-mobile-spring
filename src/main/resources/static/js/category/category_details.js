function deleteCategory() {
    showModal("Remover Categoria", "Tem certeza que deseja remover esta categoria?");
}

confirmAction = () => {
    const categoryId = document.getElementById("categoryId").value;
    fetch(`/categories/${categoryId}/delete`, {
        method: 'POST',
    }).then(response => {
        if (response.ok) {
            showModal("Sucesso", "Categoria removida com sucesso.");
            window.location.href = "/categories";
        } else {
            showModal("Erro", "Erro ao remover a categoria.");
        }
    });
};