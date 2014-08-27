package com.sshtools.ssh.components.jce;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.sshtools.ssh.SshException;
import com.sshtools.ssh.components.SshHmac;

/**
 * An abstract class that implements the {@link com.sshtools.ssh.components.SshHmac}
 * interface to provide support for JCE based message authentication.
 * 
 * @author Lee David Painter
 *
 */
public abstract class AbstractHmac implements SshHmac {

	protected Mac mac;
	protected int macSize;
	protected int macLength;
	protected String jceAlgorithm;
	
	public AbstractHmac(String jceAlgorithm, int macLength) {
		this(jceAlgorithm, macLength, macLength);
	}

	public AbstractHmac(String jceAlgorithm, int macSize, int outputLength) {
		this.jceAlgorithm = jceAlgorithm;
		this.macSize = macSize;
		this.macLength = outputLength;
	}
	
	public void generate(long sequenceNo, byte[] data, int offset, int len,
			byte[] output, int start) {
		
        byte[] sequenceBytes = new byte[4];
        sequenceBytes[0] = (byte) (sequenceNo >> 24);
        sequenceBytes[1] = (byte) (sequenceNo >> 16);
        sequenceBytes[2] = (byte) (sequenceNo >> 8);
        sequenceBytes[3] = (byte) (sequenceNo >> 0);
        mac.update(sequenceBytes);
        mac.update(data, offset, len);

        byte[] tmp = mac.doFinal();
        
        System.arraycopy(tmp, 0, output, start, macLength);

	}
	
	public void update(byte[] b) {
		mac.update(b);
	}
	
	public byte[] doFinal() {
		return mac.doFinal();
	}

	public abstract String getAlgorithm();

	public String getProvider() {
		return mac.getProvider().getName();
	}
	
	public int getMacLength() {
		return macLength;
	}

	public void init(byte[] keydata) throws SshException {
        try {
            mac = JCEProvider.getProviderForAlgorithm(jceAlgorithm)==null ? Mac.getInstance(jceAlgorithm) : Mac.getInstance(jceAlgorithm, JCEProvider.getProviderForAlgorithm(jceAlgorithm));

            // Create a key of 16 bytes
            byte[] key = new byte[macSize];
            System.arraycopy(keydata, 0, key, 0, key.length);

            SecretKeySpec keyspec = new SecretKeySpec(key, jceAlgorithm);
            mac.init(keyspec);
        } catch (Throwable t) {
            throw new SshException(t);
        }
	}

	public boolean verify(long sequenceNo, byte[] data, int start, int len,
			byte[] mac, int offset) {
        
		int length = getMacLength();
        byte[] generated = new byte[length];
        
        generate(sequenceNo, data, start, len, generated, 0);
        
        for(int i=0;i<generated.length;i++) {
        	if(mac[i+offset]!=generated[i])
        		return false;
        }
        return true;
	}

}