class HttpClient {
    constructor(baseURL = '') {
        this.baseURL = baseURL;
    }

    async request(endpoint, options = {}) {
        const url = `${this.baseURL}${endpoint}`;
        const config = {
            headers: {
                'Content-Type': 'application/json',
                ...options.headers,
            },
            ...options,
        };

        if (config.body && typeof config.body === 'object') {
            config.body = JSON.stringify(config.body);
        }

        try {
            const response = await fetch(url, config);
            
            if (!response.ok) {
                const errorData = await this.parseResponse(response);
                throw new HttpError(
                    errorData.message || 'Erro na requisição',
                    response.status,
                    errorData
                );
            }

            return await this.parseResponse(response);
        } catch (error) {
            if (error instanceof HttpError) {
                throw error;
            }
            throw new HttpError('Erro de conexão', 0, { originalError: error.message });
        }
    }

    async parseResponse(response) {
        const contentType = response.headers.get('content-type');
        if (contentType && contentType.includes('application/json')) {
            return await response.json();
        }
        return await response.text();
    }

    get(endpoint, options = {}) {
        return this.request(endpoint, { ...options, method: 'GET' });
    }

    post(endpoint, data = {}, options = {}) {
        return this.request(endpoint, { ...options, method: 'POST', body: data });
    }

    put(endpoint, data = {}, options = {}) {
        return this.request(endpoint, { ...options, method: 'PUT', body: data });
    }

    delete(endpoint, options = {}) {
        return this.request(endpoint, { ...options, method: 'DELETE' });
    }
}

class HttpError extends Error {
    constructor(message, status, data = {}) {
        super(message);
        this.name = 'HttpError';
        this.status = status;
        this.data = data;
    }
}

const httpClient = new HttpClient(window.location.origin + '/api/v1');