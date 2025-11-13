class UserService {
    constructor() {
        this.http = new HttpClient(window.location.origin + '/api/v1');
    }

    async getAll(search = '') {
        const endpoint = search ? `/users?search=${encodeURIComponent(search)}` : '/users';
        return await this.http.get(endpoint);
    }

    async getById(id) {
        return await this.http.get(`/users/${id}`);
    }

    async create(userData) {
        return await this.http.post('/users', userData);
    }

    async update(id, userData) {
        return await this.http.put(`/users/${id}`, userData);
    }

    async delete(id) {
        return await this.http.delete(`/users/${id}`);
    }
}

const userService = new UserService();