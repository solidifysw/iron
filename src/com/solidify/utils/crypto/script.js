var path = '/Users/jr1/Documents/projects/iron/src/com/solidify/utils/crypto/';
load(path+'core.js');
load(path+'x64-core.js');
load(path+'cipher-core.js');
load(path+'enc-base64.js');
load(path+'mode-cfb.js');
load(path+'sha1.js');
load(path+'sha256.js');
load(path+'hmac.js');
load(path+'pbkdf2.js');
load(path+'aes.js');
load(path+'md5.js');

    var cryptoOptions = {
        mode: CryptoJS.mode.CFB,
        padding: CryptoJS.pad.Pkcs7,
        iv: null,
        asBytes: true
    };

function updateIv(bytes) {
    if (typeof(bytes) == 'undefined') {
        cryptoOptions.iv = CryptoJS.lib.WordArray.random(16); // random IV for encryption
    } else {
        cryptoOptions.iv = CryptoJS.lib.WordArray.create(bytes); // decrypt with known IV
    }
}

function decrypt(encryptedText, groupPassword, groupId) {
    // per https://code.google.com/p/crypto-js/issues/detail?id=80
    // on the iOS mobile, decryption may randomly fail
    // best resolution is to just try it several times before failing.
    var data = undefined;
    var triesLeft = 10;
    while (data === undefined && --triesLeft > 0) {
        try {
            var encryptedBytes = CryptoJS.enc.Base64.parse(encryptedText);
            updateIv(encryptedBytes.words.splice(0, 4));
            encryptedBytes = CryptoJS.lib.WordArray.create(encryptedBytes.words);
            var decryptedBytes = CryptoJS.AES.decrypt({ciphertext: encryptedBytes}, getPassKeyBytes(groupPassword,groupId), cryptoOptions);
            data = decryptedBytes.toString(CryptoJS.enc.Utf8);
        } catch (err) {
            print('in err');
        }
    }
    if (triesLeft < 9) {
        print('it took ' + (10 - triesLeft) + ' tries to decrypt data');
    }
    return data;
};

function refresh() {
    getPassKeyBytes(true);
};

function getPassKeyBytes(groupPassword, groupId) {
    // cached for performance reasons

    try {
        var bytes = CryptoJS.PBKDF2(groupPassword,groupId, {
            keySize: 8,
            iterations: 200,
            asBytes: true
        });
    } catch (err) {
        print('error');
    }
    // server is generating a 256-bit key,
    // for some reason CryptoJS will only create in chunks of 160-bits
    // so we will just create the next largest size (320-bits), and lop off the last two words,
    // which leaves us with a 256-bit key
    bytes = CryptoJS.lib.WordArray.create(bytes.words.splice(0, bytes.words.length - 2));
    return bytes;
}