import axios from "axios";

const endPoint = process.env.REACT_APP_BACKEND_ENDPOINT;

export const commentApi = axios.create({
    baseURL : endPoint
})

const api = axios.create({
    baseURL : endPoint
});

// 요청 인터셉터: access token 헤더에 부착
api.interceptors.request.use(
    (config) => {
        const accessToken = localStorage.getItem('at');
        if (accessToken) {
            config.headers.Authorization = `Bearer ${accessToken}`;
        }
        return config;
    },
    (error) => Promise.reject(error)
);

// access token 만료시 자동 재발급하여 재요청
// api.interceptors.response.use(
//     (response) => response, 
//     async (error) => {
        
//         console.log("debug >>>>>>>> response error : ", error);
//         console.log("debug >>>>>>>> response config : ", error.config);
//         console.log("debug >>>>>>>> response response : ", error.response);
//         const originalRequest = error.config;
//         if( error.response?.status === 401 && !originalRequest._retry) {
//             originalRequest._retry = true;
//             try {
//                 const refreshToken = localStorage.getItem("refreshToken");
//                 console.log("debug >>>>>>>> refreshToken : ", refreshToken);
//                 const res = await axios.post('auth/renew', {}, {
//                                 baseURL: "http://localhost:8000/",
//                                 headers : {
//                                     Authorization : `Bearer ${refreshToken}`
//                                 }
//                             });
//                 console.log("debug >>>>>>>> response header : ", res.headers);
//                 const newAccessToken = res.headers.get("Authorization")
//                 localStorage.setItem("accessToken", newAccessToken);
//                 // 재요청            
//                 originalRequest.headers.Authorization = `Bearer ${newAccessToken}` ;
//                 return api(originalRequest);
//             } catch(err) {
//                 console.log("debug >>>> response interceptor ", err);
//                 return Promise.reject(err);
//             }
            
//         } // if end
//         return Promise.reject(error);          
//     }
// );

export default api;