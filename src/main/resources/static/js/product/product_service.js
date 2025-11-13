class ProductService {
    constructor() {
        this.http = new HttpClient(window.location.origin + '/api/v1');
    }

    async getAll(search = '') {
        const endpoint = search ? `/products?search=${encodeURIComponent(search)}` : '/products';
        return await this.http.get(endpoint);
    }

    async getById(id) {
        return await this.http.get(`/products/${id}`);
    }

    async create(productData) {
        return await this.http.post('/products', productData);
    }

    async update(id, productData) {
        return await this.http.put(`/products/${id}`, productData);
    }

    async delete(id) {
        return await this.http.delete(`/products/${id}`);
    }
}

const productService = new ProductService();