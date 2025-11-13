class CategoryService {
    constructor() {
        this.http = new HttpClient(window.location.origin + '/api/v1');
    }

    async getAll(search = '') {
        const endpoint = search ? `/categories?search=${encodeURIComponent(search)}` : '/categories';
        return await this.http.get(endpoint);
    }

    async getById(id) {
        return await this.http.get(`/categories/${id}`);
    }

    async create(categoryData) {
        return await this.http.post('/categories', categoryData);
    }

    async update(id, categoryData) {
        return await this.http.put(`/categories/${id}`, categoryData);
    }

    async delete(id) {
        return await this.http.delete(`/categories/${id}`);
    }

    async validateName(name, currentId = null) {
        try {
            const categories = await this.getAll();
            const exists = categories.some(cat => 
                cat.name.toLowerCase() === name.toLowerCase() && 
                cat.id != currentId
            );
            return !exists;
        } catch (error) {
            console.error('Erro ao validar nome:', error);
            return true;
        }
    }
}

const categoryService = new CategoryService();