function fn1(s) {
    // a<b
    // (assert (forall ((x Int)) (=> (and (>= x 1) (<= x n)) (= (h_a x) (h_b x)) (not (t_b x)))))
    var res = "";
    var code1 = '(assert (forall ((x Int)) (=> (and (>= x 1) (<= x n)) (= (h_';
    var code2 = ' x) (h_';
    var code3 = ' x)) (not (t_';
    var code4 = ' x)))))';
    var a, b;
    if (s.indexOf(code1) >= 0) {
        res = s.split(code1)[1];
        if (res.indexOf(code2) >= 0) {
            var arr = res.split(code2);
            a = arr && arr[0];
            res = arr && arr[1];
            if (res.indexOf(code3) >= 0) {
                var arr = res.split(code3);
                b = arr && arr[0];
                res = arr && arr[1];
                if (b + code4 === res) {
                    return `${a}<${b}`;
                }
            }
        }
    }
    return false;
}

function fn2(s) {
    // a≤b
    // (assert (forall ((x Int)) (=> (and (>= x 1) (<= x (+ n 1))) (>= (h_a x) (h_b x)))))
    var res = "";
    var code1 = '(assert (forall ((x Int)) (=> (and (>= x 1) (<= x (+ n 1))) (>= (h_';
    var code2 = ' x) (h_';
    var code3 = ' x)))))';
    var a, b;
    if (s.indexOf(code1) >= 0) {
        res = s.split(code1)[1];
        if (res.indexOf(code2) >= 0) {
            var arr = res.split(code2);
            a = arr && arr[0];
            res = arr && arr[1];
            if (res.indexOf(code3) >= 0) {
                var arr = res.split(code3);
                b = arr && arr[0];
                return `${a}≤${b}`;
            }
        }
    }
    return false;
}

function fn3(s) {
    // a⊆b
    // (assert (forall ((x Int)) (=> (and (>= x 1) (<= x n)) (=> (t_a x) (t_b x)))))
    var res = "";
    var code1 = '(assert (forall ((x Int)) (=> (and (>= x 1) (<= x n)) (=> (t_';
    var code2 = ' x) (t_';
    var code3 = ' x)))))';
    var a, b;
    if (s.indexOf(code1) >= 0) {
        res = s.split(code1)[1];
        if (res.indexOf(code2) >= 0) {
            var arr = res.split(code2);
            a = arr && arr[0];
            res = arr && arr[1];
            if (res.indexOf(code3) >= 0) {
                var arr = res.split(code3);
                b = arr && arr[0];
                return `${a}⊆${b}`;
            }
        }
    }
    return false;
}

function fn4(s) {
    // a#b
    // (assert (forall ((x Int)) (=> (and (>= x 1) (<= x n)) (or (not (t_a x)) (not (t_b x))))))
    var res = "";
    var code1 = '(assert (forall ((x Int)) (=> (and (>= x 1) (<= x n)) (or (not (t_';
    var code2 = ' x)) (not (t_';
    var code3 = ' x))))))';
    var a, b;
    if (s.indexOf(code1) >= 0) {
        res = s.split(code1)[1];
        if (res.indexOf(code2) >= 0) {
            var arr = res.split(code2);
            a = arr && arr[0];
            res = arr && arr[1];
            if (res.indexOf(code3) >= 0) {
                var arr = res.split(code3);
                b = arr && arr[0];
                return `${a}#${b}`;
            }
        }
    }
    return false;
}


function fn5(s) {
    // c=a+b
    // (assert (forall ((x Int)) (=> (and (>= x 1) (<= x n)) (= (t_c x) (or (t_a x) (t_b x))))))
    var res = "";
    var code1 = '(assert (forall ((x Int)) (=> (and (>= x 1) (<= x n)) (= (t_';
    var code2 = ' x) (or (t_';
    var code3 = ' x) (t_';
    var code4 = ' x))))))';
    var a, b, c;
    if (s.indexOf(code1) >= 0) {
        res = s.split(code1)[1];
        if (res.indexOf(code2) >= 0) {
            var arr = res.split(code2);
            c = arr && arr[0];
            res = arr && arr[1];
            if (res.indexOf(code3) >= 0) {
                var arr = res.split(code3);
                a = arr && arr[0];
                res = arr && arr[1];
                if (res.indexOf(code4) >= 0) {
                    var arr = res.split(code4);
                    b = arr && arr[0];
                    return `${c}=${a}+${b}`;
                }
            }
        }
    }
    return false;
}


function fn6(s) {
    // c=a*b
    // (assert (forall ((x Int)) (=> (and (>= x 1) (<= x n)) (= (t_c x) (and (t_a x) (t_b x))))))
    var res = "";
    var code1 = '(assert (forall ((x Int)) (=> (and (>= x 1) (<= x n)) (= (t_';
    var code2 = ' x) (and (t_';
    var code3 = ' x) (t_';
    var code4 = ' x))))))';
    var a, b, c;
    if (s.indexOf(code1) >= 0) {
        res = s.split(code1)[1];
        if (res.indexOf(code2) >= 0) {
            var arr = res.split(code2);
            c = arr && arr[0];
            res = arr && arr[1];
            if (res.indexOf(code3) >= 0) {
                var arr = res.split(code3);
                a = arr && arr[0];
                res = arr && arr[1];
                if (res.indexOf(code4) >= 0) {
                    var arr = res.split(code4);
                    b = arr && arr[0];
                    return `${c}=${a}*${b}`;
                }
            }
        }
    }
    return false;
}


function fn7(s) {
    // c=a∧b:
    /**
     * (assert (forall ((x Int)) (=> (and (>= x 1) (<= x (+ n 1))) (ite (>= (h_
     * a
     *  x) (h_
     * b
     *  x)) (= (h_
     * c
     *  x) (h_a x)) (= (h_c x) (h_b x))))))
     */
    var res = "";
    var code1 = '(assert (forall ((x Int)) (=> (and (>= x 1) (<= x (+ n 1))) (ite (>= (h_';
    var code2 = ' x) (h_';
    var code3 = ' x)) (= (h_';
    var code4 = ' x))))))';
    var a, b, c;
    if (s.indexOf(code1) >= 0) {
        res = s.split(code1)[1];
        if (res.indexOf(code2) >= 0) {
            var arr = res.split(code2);
            a = arr && arr[0];
            res = arr && arr.slice(1).join(code2);
            if (res.indexOf(code3) >= 0) {
                var arr = res.split(code3);
                b = arr && arr[0];
                res = arr && arr.slice(1).join(code3);
                if (res.indexOf(code2) >= 0) {
                    var arr = res.split(code2);
                    c = arr && arr[0];
                    res = arr && arr.slice(1).join(code2);
                    if (a + code3 + c + code2 + b + code4 === res) {
                        return `${c}=${a}∧${b}`;
                    }
                }
            }
        }
    }
    return false;
}



function fn8(s) {
    // c=a∨b:
    /**
     * (assert (forall ((x Int)) (=> (and (>= x 1) (<= x (+ n 1))) (ite (>= (h_a x) (h_b x)) (= (h_c x) (h_b x)) (= (h_c x) (h_a x))))))
     */
    var res = "";
    var code1 = '(assert (forall ((x Int)) (=> (and (>= x 1) (<= x (+ n 1))) (ite (>= (h_';
    var code2 = ' x) (h_';
    var code3 = ' x)) (= (h_';
    var code4 = ' x))))))';
    var a, b, c;
    if (s.indexOf(code1) >= 0) {
        res = s.split(code1)[1];
        if (res.indexOf(code2) >= 0) {
            var arr = res.split(code2);
            a = arr && arr[0];
            res = arr && arr.slice(1).join(code2);
            if (res.indexOf(code3) >= 0) {
                var arr = res.split(code3);
                b = arr && arr[0];
                res = arr && arr.slice(1).join(code3);
                if (res.indexOf(code2) >= 0) {
                    var arr = res.split(code2);
                    c = arr && arr[0];
                    res = arr && arr.slice(1).join(code2);
                    if (b + code3 + c + code2 + a + code4 === res) {
                        return `${c}=${a}∨${b}`;
                    }
                }
            }
        }
    }
    return false;
}



function fn9(s) {
    /**
     * c=a$d:
     * (assert (forall ((x Int)) (=> (and (>= x 1) (<= x (+ n 1))) (ite (>= (h_a x) d) (= (h_c x) (- (h_
     * a x) d)) (= (h_c x) 0)))))
     */
    var res = "";
    var code1 = '(assert (forall ((x Int)) (=> (and (>= x 1) (<= x (+ n 1))) (ite (>= (h_';
    var code2 = ' x) ';
    var code3 = ') (= (h_';
    var code4 = ' x) (- (h_';
    var code5 = ')) (= (h_';
    var code6 = ' x) 0)))))';
    var a, b, c, d;
    if (s.indexOf(code1) >= 0) {
        res = s.split(code1)[1];
        if (res.indexOf(code2) >= 0) {
            var arr = res.split(code2);
            a = arr && arr[0];
            res = arr && arr.slice(1).join(code2);
            if (res.indexOf(code3) >= 0) {
                var arr = res.split(code3);
                d = arr && arr[0];
                res = arr && arr.slice(1).join(code3);
                if (res.indexOf(code4) >= 0) {
                    var arr = res.split(code4);
                    c = arr && arr[0];
                    res = arr && arr.slice(1).join(code4);
                    if (a + code2 + d + code5 + c + code6 === res) {
                        return `${c}=${a}$${d}`;
                    }
                }
            }
        }
    }
    return false;
}


function fn10(s) {
    /**
     * c=a$d on b:
     * (assert (forall ((x Int)) (=> (and (>= x 1) (<= x n)) (= (t_
     * c
     *  x) (and (t_
     * b
     *  x) (exists ((m Int)) (and (>= m 1) (<= m x) (t_
     * a
     *  m) (= (- (h_
     * b
     *  x) (h_
     * b
     *  m)) 
     * d
     * ))))))))
     */
    //(assert (forall ((x Int)) (=> (and (>= x 1) (<= x n)) (= (t_
    //c x) (and (t_b x) (exists ((m Int)) (and (>= m 1) (<= m x) (t_b m) (= (- (h_b x) (h_b m)) 1) (>= (- (h_a x) (h_a m)) 1))))))))
    var res = "";
    var code1 = '(assert (forall ((x Int)) (=> (and (>= x 1) (<= x n)) (= (t_';
    var code2 = ' x) (and (t_';
    var code3 = ' x) (exists ((m Int)) (and (>= m 1) (<= m x) (t_';
    var code4 = ' m) (= (- (h_';
    var code5 = ' x) (h_';
    var code6 = ' m)) ';
    var code7 = '))))))))';
    var a, b, c, d;
    if (s.indexOf(code1) >= 0) {
        res = s.split(code1)[1];
        if (res.indexOf(code2) >= 0) {
            var arr = res.split(code2);
            c = arr && arr[0];
            res = arr && arr.slice(1).join(code2);
            if (res.indexOf(code3) >= 0) {
                var arr = res.split(code3);
                b = arr && arr[0];
                res = arr && arr.slice(1).join(code3);
                if (res.indexOf(code4) >= 0) {
                    var arr = res.split(code4);
                    a = arr && arr[0];
                    res = arr && arr.slice(1).join(code4);
                    if(res.indexOf(b + code5 + b + code6) >= 0) {
                        res = s.split(b + code5 + b + code6)[1];
                        if (res.indexOf(code7) >= 0) {
                            var arr = res.split(code7);
                            d = arr && arr[0];
                            console.log(arr[1])
                            if (arr[1] === '') {return `${c}=${a}$${d} on ${b}`;}
                        }
                    }
                }
            }
        }
    }
    return false;
}



function fn11(s) {
    /**
     * c=a∝d:
     * (assert (forall ((x Int)) (=> (and (>= x 1) (<= x n)) (= (t_
     * c
     *  x) (and (t_
     * a
     *  x) (> (h_
     * a
     *  x) 0) (= (mod (h_
     * a
     *  x) 
     * d
     * ) 0))))))
     */
    var res = "";
    var code1 = '(assert (forall ((x Int)) (=> (and (>= x 1) (<= x n)) (= (t_';
    var code2 = ' x) (and (t_';
    var code3 = ' x) (> (h_';
    var code4 = ' x) 0) (= (mod (h_';
    var code5 = ' x) ';
    var code6 = ') 0))))))';
    var a, b, c, d;
    if (s.indexOf(code1) >= 0) {
        res = s.split(code1)[1];
        if (res.indexOf(code2) >= 0) {
            var arr = res.split(code2);
            c = arr && arr[0];
            res = arr && arr.slice(1).join(code2);
            if (res.indexOf(code3) >= 0) {
                var arr = res.split(code3);
                a = arr && arr[0];
                res = arr && arr.slice(1).join(code3);
                if (res.indexOf(a + code4 + a + code5) >= 0) {
                    res = s.split(a + code4 + a + code5)[1];
                    if (res.indexOf(code6) >= 0) {
                        var arr = res.split(code6);
                        d = arr && arr[0];
                        return `${c}=${a}∝${d}`;
                    }
                }
            }
        }
    }
    return false;
}


function fn12(s) {
    /**
     * c=a☇b:
     * (assert (forall ((x Int)) (=> (and (>= x 1) (<= x n)) (= (t_
     * c
     *  x) (and (t_
     * b
     *  x) (exists ((m Int)) (and (>= m 1) (<= m x) (t_
     * b
     *  m) (= (- (h_
     * b
     *  x) (h_
     * b
     *  m)) 1) (>= (- (h_
     * a
     *  x) (h_
     * a
     *  m)) 1))))))))
     */
    var res = "";
    var code1 = '(assert (forall ((x Int)) (=> (and (>= x 1) (<= x n)) (= (t_';
    var code2 = ' x) (and (t_';
    var code3 = ' x) (exists ((m Int)) (and (>= m 1) (<= m x) (t_';
    var code4 = ' m) (= (- (h_';
    var code5 = ' x) (h_';
    var code6 = ' m)) 1) (>= (- (h_';
    var code7 = ' m)) 1))))))))';
    var a, b, c, d;
    if (s.indexOf(code1) >= 0) {
        res = s.split(code1)[1];
        if (res.indexOf(code2) >= 0) {
            var arr = res.split(code2);
            c = arr && arr[0];
            res = arr && arr.slice(1).join(code2);
            if (res.indexOf(code3) >= 0) {
                var arr = res.split(code3);
                b = arr && arr[0];
                res = arr && arr.slice(1).join(code3);
                if (res.indexOf(b + code4 + b + code5 + b + code6) >= 0) {
                    res = s.split(b + code4 + b + code5 + b + code6)[1];
                    if (res.indexOf(code5) >= 0) {
                        var arr = res.split(code5);
                        a = arr && arr[0];
                        res = arr && arr.slice(1).join(code5);
                        if (res.indexOf(a + code7) >= 0) {
                             return `${c}=${a}☇${b}`;
                        }
                    }
                }
            }
        }
    }
    return false;
}

function fn13(s) {
    // a=b
    // (assert (forall ((x Int)) (=> (and (>= x 1) (<= x n)) (= (t_a x) (t_b x)))))
    var res = "";
    var code1 = '(assert (forall ((x Int)) (=> (and (>= x 1) (<= x n)) (= (t_';
    var code2 = ' x) (t_';
    var code3 = ' x)))))';
    var a, b;
    if (s.indexOf(code1) >= 0) {
        res = s.split(code1)[1];
        if (res.indexOf(code2) >= 0) {
            var arr = res.split(code2);
            a = arr && arr[0];
            res = arr && arr[1];
            if (res.indexOf(code3) >= 0) {
                var arr = res.split(code3);
                b = arr && arr[0];
                return `${a}==${b}`;
            }
        }
    }
    return false;
}

function parse(s) {
    return fn1(s) || fn2(s) || fn3(s) || fn4(s) || fn5(s) || fn6(s) ||
     fn7(s) || fn8(s) || fn9(s) || fn12(s) || fn10(s) || fn11(s)  || fn13(s) ;
}


// exports.smt = function (s, f) {
//     var arr = s.split('(assert') && s.split('(assert').slice(1).map(a => {
//         if (a.charAt(a.length -1) !== ')') a = a.slice(0, a.length - 1)
//         return '(assert' + a;
//     });
//     return arr.reduce((all, a) => all += parse(a) + '<br/>', '');

// }


